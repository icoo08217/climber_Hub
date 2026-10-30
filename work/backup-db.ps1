$ErrorActionPreference = 'Stop'
$configText = Get-Content -LiteralPath 'C:\dev\src\main\resources\application-secret.yaml' -Raw
function Read-DbValue([string]$key) {
    $match = [regex]::Match($configText, '(?m)^\s*' + [regex]::Escape($key) + ':\s*(.*?)\s*$')
    if (-not $match.Success) { throw 'Missing DB setting' }
    return $match.Groups[1].Value.Trim().Trim([char]39,[char]34)
}
$jdbcUrl = Read-DbValue 'url'
if ($jdbcUrl -notmatch '^jdbc:mysql://([^:/?]+)(?::(\d+))?/([^?]+)') { throw 'Unexpected JDBC URL' }
$dbHostName = $Matches[1]
$dbPortNumber = if ($Matches[2]) { $Matches[2] } else { '3306' }
$dbSchemaName = $Matches[3]
if ($dbSchemaName -ne 'climber_hub' -or $dbHostName -notin @('localhost','127.0.0.1')) { throw 'Unexpected database' }
$dbAccountName = Read-DbValue 'username'
$backupDirectory = 'C:\dev\work\db-backups'
New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
$backupPath = Join-Path $backupDirectory ('climber_hub_before_social_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.sql')
$env:MYSQL_PWD = Read-DbValue 'password'
try {
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe' "--host=$dbHostName" "--port=$dbPortNumber" "--user=$dbAccountName" --single-transaction --no-tablespaces --set-gtid-purged=OFF --default-character-set=utf8mb4 "--result-file=$backupPath" $dbSchemaName
    if ($LASTEXITCODE -ne 0) { throw 'Backup failed' }
    Get-Item -LiteralPath $backupPath | Select-Object FullName, Length
} finally { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
