param([switch]$PreviewOnly)

$ErrorActionPreference = 'Stop'

$directoryUrl = 'https://climbmap.life/places/kr/seoul/indoor'
$expectedPlaceCount = 66
$sourceCheckedDate = '2026-11-27'
$mysqlPath = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

function Read-DbValue([string]$configText, [string]$key) {
    $match = [regex]::Match($configText, '(?m)^\s*' + [regex]::Escape($key) + ':\s*(.*?)\s*$')
    if (-not $match.Success) { throw "Missing datasource setting: $key" }
    return $match.Groups[1].Value.Trim().Trim([char]39, [char]34)
}

function Read-Attribute([string]$attributes, [string]$name) {
    $match = [regex]::Match($attributes, 'data-' + [regex]::Escape($name) + '="([^"]*)"')
    if ($match.Success) { return [System.Net.WebUtility]::HtmlDecode($match.Groups[1].Value) }
    return ''
}

function Convert-Schedule([string]$encodedSchedule) {
    if ([string]::IsNullOrWhiteSpace($encodedSchedule)) { return $null }
    try {
        $schedule = [uri]::UnescapeDataString($encodedSchedule) | ConvertFrom-Json
        $days = [ordered]@{ mon = '월'; tue = '화'; wed = '수'; thu = '목'; fri = '금'; sat = '토'; sun = '일' }
        $parts = foreach ($key in $days.Keys) {
            $value = $schedule.$key
            if ($value -and $value.open -and $value.close) {
                $days[$key] + ' ' + $value.open + '–' + $value.close
            }
        }
        if ($parts.Count -gt 0) { return $parts -join '; ' }
    } catch {
        throw 'Invalid schedule data from source page'
    }
    return $null
}

function Convert-Discipline([string]$disciplines) {
    $translated = $disciplines.Replace('bouldering', '볼더링').Replace('lead', '리드').Replace('speed', '스피드')
    return '서울 ' + $translated.Replace('|', '·') + ' 클라이밍 시설'
}

function Convert-SqlText($value) {
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) { return 'NULL' }
    $escaped = ([string]$value).Replace('\', '\\').Replace("'", "''")
    return "'$escaped'"
}

function Assert-Text([string]$value, [string]$field, [int]$maxLength) {
    if ([string]::IsNullOrWhiteSpace($value)) { throw "$field is required" }
    if ($value.Length -gt $maxLength) { throw "$field exceeds $maxLength characters" }
    if ($value -match '[\x00-\x08\x0B\x0C\x0E-\x1F]') { throw "$field contains control characters" }
}

$directoryHtml = (Invoke-WebRequest -Uri $directoryUrl -UseBasicParsing -TimeoutSec 30).Content
$cardPattern = '(?s)<li data-place-card(?<attrs>.*?)><a class="place-card-link" href="(?<href>[^"]+)"><span><strong>(?<name>[^<]+)</strong><small>(?<address>[^<]+)</small>'
$cards = @([regex]::Matches($directoryHtml, $cardPattern) | ForEach-Object {
    $attributes = $_.Groups['attrs'].Value
    [pscustomobject]@{
        href = $_.Groups['href'].Value
        externalId = [uri]::UnescapeDataString(($_.Groups['href'].Value -split '/')[-1])
        name = [System.Net.WebUtility]::HtmlDecode($_.Groups['name'].Value)
        address = [System.Net.WebUtility]::HtmlDecode($_.Groups['address'].Value)
        latitude = [double](Read-Attribute $attributes 'lat')
        longitude = [double](Read-Attribute $attributes 'lng')
        disciplines = Read-Attribute $attributes 'disciplines'
        schedule = Read-Attribute $attributes 'schedule'
    }
})

if ($cards.Count -ne $expectedPlaceCount) {
    throw "Expected $expectedPlaceCount Seoul gyms but found $($cards.Count). Source review required."
}

$duplicateExternalIds = @($cards | Group-Object externalId | Where-Object Count -gt 1)
$duplicateAddresses = @($cards | Group-Object address | Where-Object Count -gt 1)
if ($duplicateExternalIds.Count -gt 0) { throw 'Duplicate external IDs found in source data' }
if ($duplicateAddresses.Count -gt 0) { throw 'Duplicate addresses found in source data' }

foreach ($card in $cards) {
    Assert-Text $card.name 'name' 255
    Assert-Text $card.address 'address' 255
    Assert-Text $card.externalId 'externalId' 255
    if ($card.latitude -lt 37.4 -or $card.latitude -gt 37.75 -or
        $card.longitude -lt 126.75 -or $card.longitude -gt 127.2) {
        throw "Coordinate outside Seoul bounds: $($card.name)"
    }
}

$rows = @($cards | ForEach-Object -Parallel {
    $card = $_
    $pageUrl = 'https://climbmap.life' + $card.href
    try {
        $page = (Invoke-WebRequest -Uri $pageUrl -UseBasicParsing -TimeoutSec 30).Content
    } catch {
        throw "Failed to load place page: $pageUrl"
    }
    $phoneMatch = [regex]::Match($page, '"telephone":"([^"]+)"')
    $phone = if ($phoneMatch.Success) { $phoneMatch.Groups[1].Value } else { $null }
    [pscustomobject]@{
        externalId = $card.externalId
        name = $card.name
        address = $card.address
        phone = $phone
        latitude = $card.latitude
        longitude = $card.longitude
        disciplines = $card.disciplines
        schedule = $card.schedule
        verified = $page -match '운영 검수 완료'
    }
} -ThrottleLimit 8 | Sort-Object name)

$verifiedCount = @($rows | Where-Object verified).Count
$pendingCount = $rows.Count - $verifiedCount
if ($verifiedCount -lt 1) { throw 'No verified gyms were found' }

$values = foreach ($row in $rows) {
    $description = Convert-Discipline $row.disciplines
    $openHours = Convert-Schedule $row.schedule
    $status = if ($row.verified) { 'VERIFIED' } else { 'PENDING' }
    $active = if ($row.verified) { "b'1'" } else { "b'0'" }
    $verifiedAt = if ($row.verified) { 'CURRENT_TIMESTAMP(6)' } else { 'NULL' }
    $fields = @(
        (Convert-SqlText $row.name),
        (Convert-SqlText $description),
        (Convert-SqlText $row.address),
        (Convert-SqlText $row.phone),
        $row.latitude.ToString([Globalization.CultureInfo]::InvariantCulture),
        $row.longitude.ToString([Globalization.CultureInfo]::InvariantCulture),
        (Convert-SqlText $openHours),
        'NULL',
        "'CURATED'",
        (Convert-SqlText $row.externalId),
        (Convert-SqlText $status),
        $active,
        $verifiedAt,
        'CURRENT_TIMESTAMP(6)',
        "'seoul-gym-import-$sourceCheckedDate'",
        'CURRENT_TIMESTAMP(6)',
        "'seoul-gym-import-$sourceCheckedDate'"
    )
    '(' + ($fields -join ',') + ')'
}

$insertSql = @"
INSERT INTO gym (
  name, description, address, phone, latitude, longitude, open_hours, pricing_info,
  external_source, external_id, verification_status, active, verified_at,
  created_at, created_by, updated_at, updated_by
) VALUES
$($values -join ",`n");
"@

if ($PreviewOnly) {
    $phoneCount = @($rows | Where-Object { -not [string]::IsNullOrWhiteSpace($_.phone) }).Count
    Write-Output "Preview=$($rows.Count) Verified=$verifiedCount Pending=$pendingCount Phones=$phoneCount SqlLength=$($insertSql.Length)"
    exit 0
}

$configText = Get-Content -LiteralPath 'C:\dev\src\main\resources\application-secret.yaml' -Raw
$jdbcUrl = Read-DbValue $configText 'url'
if ($jdbcUrl -notmatch '^jdbc:mysql://([^:/?]+)(?::(\d+))?/([^?]+)') { throw 'Unsupported JDBC URL' }
$dbHostName = $Matches[1]
$dbPortNumber = if ($Matches[2]) { $Matches[2] } else { '3306' }
$dbSchemaName = $Matches[3]
$isLocal = $dbHostName -in @('localhost', '127.0.0.1')
$isAiven = $dbHostName.EndsWith('.aivencloud.com')
if (-not ($isLocal -or $isAiven)) { throw 'Database host is not allowlisted' }
if ($dbSchemaName -notin @('climber_hub', 'climber-hub')) { throw 'Database schema is not allowlisted' }

$arguments = @(
    "--host=$dbHostName",
    "--port=$dbPortNumber",
    "--user=$(Read-DbValue $configText 'username')",
    "--database=$dbSchemaName",
    '--default-character-set=utf8mb4',
    '--connect-timeout=10',
    '--batch',
    '--skip-column-names'
)
if ($isAiven) { $arguments += '--ssl-mode=REQUIRED' }

$env:MYSQL_PWD = Read-DbValue $configText 'password'
try {
    $existingCount = & $mysqlPath @arguments '--execute=SELECT COUNT(*) FROM gym;'
    if ($LASTEXITCODE -ne 0) { throw 'Failed to read existing gym count' }
    if ([int]($existingCount | Select-Object -Last 1) -ne 0) {
        throw 'gym table is not empty; import stopped to prevent duplicate or overwritten data'
    }
    & $mysqlPath @arguments "--execute=$insertSql"
    if ($LASTEXITCODE -ne 0) { throw 'Gym import failed' }
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Output "Imported=$($rows.Count) Verified=$verifiedCount Pending=$pendingCount SourceChecked=$sourceCheckedDate"
