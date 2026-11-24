$ErrorActionPreference = 'Stop'
$configText = Get-Content -LiteralPath 'C:\dev\src\main\resources\application-secret.yaml' -Raw

function Read-DbValue([string]$key) {
    $match = [regex]::Match($configText, '(?m)^\s*' + [regex]::Escape($key) + ':\s*(.*?)\s*$')
    if (-not $match.Success) { throw "Missing datasource setting: $key" }
    return $match.Groups[1].Value.Trim().Trim([char]39, [char]34)
}

$jdbcUrl = Read-DbValue 'url'
if ($jdbcUrl -notmatch '^jdbc:mysql://([^:/?]+)(?::(\d+))?/([^?]+)') { throw 'Unsupported JDBC URL' }
$dbHostName = $Matches[1]
$dbPortNumber = if ($Matches[2]) { $Matches[2] } else { '3306' }
$dbSchemaName = $Matches[3]
if (-not $dbHostName.EndsWith('.aivencloud.com')) { throw 'Expected the Aiven database' }
if ($dbSchemaName -notin @('climber_hub', 'climber-hub')) { throw 'Unexpected schema' }

$env:MYSQL_PWD = Read-DbValue 'password'
try {
    $url = & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' `
        "--host=$dbHostName" "--port=$dbPortNumber" "--user=$(Read-DbValue 'username')" `
        "--database=$dbSchemaName" --ssl-mode=REQUIRED --batch --skip-column-names --raw `
        '--execute=SELECT media_url FROM post_media ORDER BY media_id DESC LIMIT 1'
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($url)) { throw 'No media URL found' }
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

$mediaUri = [Uri]$url.Trim()
if ($mediaUri.Scheme -ne 'https' -or $mediaUri.Host -ne 'res.cloudinary.com') { throw 'Unexpected media URL' }
$response = Invoke-WebRequest -Uri $mediaUri -Method Head -MaximumRedirection 3 -UseBasicParsing
[pscustomobject]@{
    StatusCode = [int]$response.StatusCode
    Host = $mediaUri.Host
    ContentType = $response.Headers.'Content-Type'
    ContentLength = $response.Headers.'Content-Length'
}
