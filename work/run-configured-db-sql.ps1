param([Parameter(Mandatory = $true)][string]$SqlPath)
$ErrorActionPreference = 'Stop'
$resolvedSqlPath = (Resolve-Path -LiteralPath $SqlPath).Path
if (-not $resolvedSqlPath.StartsWith('C:\dev\schema\', [System.StringComparison]::OrdinalIgnoreCase)) {
    throw 'SQL file must be inside C:\dev\schema'
}
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
$isLocal = $dbHostName -in @('localhost', '127.0.0.1')
$isAiven = $dbHostName.EndsWith('.aivencloud.com')
if (-not ($isLocal -or $isAiven)) { throw 'Database host is not allowlisted' }
if ($dbSchemaName -notin @('climber_hub', 'climber-hub')) { throw 'Database schema is not allowlisted' }

$arguments = @(
    "--host=$dbHostName",
    "--port=$dbPortNumber",
    "--user=$(Read-DbValue 'username')",
    "--database=$dbSchemaName",
    '--default-character-set=utf8mb4',
    '--connect-timeout=10',
    '--batch'
)
if ($isAiven) { $arguments += '--ssl-mode=REQUIRED' }
$query = Get-Content -LiteralPath $resolvedSqlPath -Raw -Encoding UTF8
$arguments += "--execute=$query"

$env:MYSQL_PWD = Read-DbValue 'password'
try {
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' @arguments
    if ($LASTEXITCODE -ne 0) { throw 'MySQL query failed' }
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
