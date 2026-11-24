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
$isLocal = $dbHostName -in @('localhost', '127.0.0.1')
$isAiven = $dbHostName.EndsWith('.aivencloud.com')
if (-not ($isLocal -or $isAiven)) { throw 'Database host is not allowlisted' }
if ($dbSchemaName -notin @('climber_hub', 'climber-hub')) { throw 'Database schema is not allowlisted' }

$dbAccountName = Read-DbValue 'username'
$backupDirectory = 'C:\dev\work\db-backups'
New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
$safeSchema = $dbSchemaName.Replace('-', '_')
$backupPath = Join-Path $backupDirectory ($safeSchema + '_before_media_comments_' + (Get-Date -Format 'yyyyMMdd_HHmmss') + '.sql')
$arguments = @(
    "--host=$dbHostName",
    "--port=$dbPortNumber",
    "--user=$dbAccountName",
    '--single-transaction',
    '--no-tablespaces',
    '--set-gtid-purged=OFF',
    '--default-character-set=utf8mb4',
    "--result-file=$backupPath"
)
if ($isAiven) { $arguments += '--ssl-mode=REQUIRED' }
$arguments += $dbSchemaName

$env:MYSQL_PWD = Read-DbValue 'password'
try {
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe' @arguments
    if ($LASTEXITCODE -ne 0) { throw 'Backup failed' }
    Get-Item -LiteralPath $backupPath | Select-Object FullName, Length
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
