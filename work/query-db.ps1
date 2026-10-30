param([Parameter(Mandatory=$true)][string]$SqlPath)
$ErrorActionPreference = 'Stop'
$configText = Get-Content -LiteralPath 'C:\dev\src\main\resources\application-secret.yaml' -Raw
function Read-DbValue([string]$key) {
    $match = [regex]::Match($configText, '(?m)^\s*' + [regex]::Escape($key) + ':\s*(.*?)\s*$')
    if (-not $match.Success) { throw "Missing datasource setting: $key" }
    return $match.Groups[1].Value.Trim().Trim([char]39,[char]34)
}
$jdbcUrl = Read-DbValue 'url'
if ($jdbcUrl -notmatch '^jdbc:mysql://([^:/?]+)(?::(\d+))?/([^?]+)') { throw 'Unsupported JDBC URL' }
$dbHostName = $Matches[1]
$dbPortNumber = if ($Matches[2]) { $Matches[2] } else { '3306' }
$dbSchemaName = $Matches[3]
if ($dbSchemaName -ne 'climber_hub' -or $dbHostName -notin @('localhost','127.0.0.1')) { throw 'Unexpected target database' }
$dbAccountName = Read-DbValue 'username'
$env:MYSQL_PWD = Read-DbValue 'password'
try {
    $query = Get-Content -LiteralPath $SqlPath -Raw -Encoding UTF8
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' "--host=$dbHostName" "--port=$dbPortNumber" "--user=$dbAccountName" "--database=$dbSchemaName" --default-character-set=utf8mb4 --connect-timeout=5 --batch "--execute=$query"
    if ($LASTEXITCODE -ne 0) { throw 'MySQL query failed' }
} finally { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
