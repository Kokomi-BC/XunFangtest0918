# ============================================================
# 脚本：call_api.ps1
# 功能：使用 X-Auth-Token 调用华为云 API
# 用法：
#   .\call_api.ps1
#   .\call_api.ps1 -ApiUrl "https://xxx.myhuaweicloud.com/v1/xxx" -Method "GET"
#   .\call_api.ps1 -ApiUrl "https://xxx.myhuaweicloud.com/v1/xxx" -Method "POST" -Body '{"key":"value"}'
# ============================================================

param(
    [string]$ApiUrl = "",
    [string]$Method = "GET",
    [string]$Body = ""
)

$ErrorActionPreference = "Stop"

# -------------------------------------------------------
# 1. 加载已保存的 Token
# -------------------------------------------------------
$tokenFile = Join-Path $PSScriptRoot "token_response.json"

if (-not (Test-Path $tokenFile)) {
    Write-Host "❌ 未找到 Token 文件：$tokenFile" -ForegroundColor Red
    Write-Host "请先运行 get_token.ps1 获取 Token" -ForegroundColor Yellow
    exit 1
}

$tokenData = Get-Content -Path $tokenFile -Raw -Encoding UTF8 | ConvertFrom-Json
$xAuthToken = $tokenData.X_Auth_Token

# 检查 Token 是否过期
$expiresAt = [DateTime]::Parse($tokenData.ExpiresAt)
$now = [DateTime]::UtcNow

Write-Host "📋 Token 信息：" -ForegroundColor Cyan
Write-Host "   用户     ：$($tokenData.UserName)" -ForegroundColor White
Write-Host "   账号     ：$($tokenData.DomainName)" -ForegroundColor White
Write-Host "   过期时间 ：$($tokenData.ExpiresAt)" -ForegroundColor White

if ($now -gt $expiresAt) {
    Write-Host "`n⚠️  Token 已过期！请重新运行 get_token.ps1 获取新 Token" -ForegroundColor Red
    exit 1
}

$remaining = $expiresAt - $now
Write-Host "   剩余有效 ：$($remaining.Hours) 小时 $($remaining.Minutes) 分钟" -ForegroundColor Green

# -------------------------------------------------------
# 2. 交互式选择要调用的 API（未指定参数时）
# -------------------------------------------------------
if (-not $ApiUrl) {
    Write-Host "`n" -NoNewline
    Write-Host ("=" * 60) -ForegroundColor DarkGray
    Write-Host "  请选择要调用的华为云 API" -ForegroundColor Yellow
    Write-Host ("=" * 60) -ForegroundColor DarkGray
    Write-Host "  1. IAM - 查询用户列表 (GET /v3/users)"
    Write-Host "  2. IAM - 查询账号信息 (GET /v3/auth/domains)"
    Write-Host "  3. ECS - 查询云服务器列表"
    Write-Host "  4. VPC - 查询 VPC 列表"
    Write-Host "  5. 自定义 API"
    Write-Host ("=" * 60) -ForegroundColor DarkGray

    $choice = Read-Host "请输入选项 (1-5)"

    switch ($choice) {
        "1" {
            $ApiUrl = "https://iam.myhuaweicloud.com/v3/users"
            $Method = "GET"
        }
        "2" {
            $ApiUrl = "https://iam.myhuaweicloud.com/v3/auth/domains"
            $Method = "GET"
        }
        "3" {
            # ECS 需要指定 region 和 project_id，此处仅示例
            Write-Host "⚠️  ECS API 需要指定 region 和 project_id" -ForegroundColor Yellow
            $region = Read-Host "请输入 Region（如 cn-south-1）"
            $projectId = Read-Host "请输入 Project ID"
            $ApiUrl = "https://ecs.$region.myhuaweicloud.com/v1/$projectId/cloudservers"
            $Method = "GET"
        }
        "4" {
            $region = Read-Host "请输入 Region（如 cn-south-1）"
            $projectId = Read-Host "请输入 Project ID"
            $ApiUrl = "https://vpc.$region.myhuaweicloud.com/v1/$projectId/vpcs"
            $Method = "GET"
        }
        "5" {
            $ApiUrl = Read-Host "请输入完整 API URL"
            $Method = Read-Host "请输入 HTTP 方法 (GET/POST/PUT/DELETE)"
            $bodyChoice = Read-Host "是否需要请求体？(y/n)"
            if ($bodyChoice -eq "y") {
                $Body = Read-Host "请输入请求体 JSON"
            }
        }
        default {
            Write-Host "❌ 无效选项" -ForegroundColor Red
            exit 1
        }
    }
}

# -------------------------------------------------------
# 3. 构建请求头（关键：X-Auth-Token）
# -------------------------------------------------------
$headers = @{
    "X-Auth-Token" = $xAuthToken
    "Content-Type" = "application/json; charset=utf-8"
}

# -------------------------------------------------------
# 4. 发送 API 请求
# -------------------------------------------------------
Write-Host "`n🌐 调用 API：" -ForegroundColor Cyan
Write-Host "   URL    ：$Method $ApiUrl" -ForegroundColor White
Write-Host "   Token  ：X-Auth-Token = $($xAuthToken.Substring(0, [Math]::Min(30, $xAuthToken.Length)))..." -ForegroundColor Gray

$params = @{
    Uri         = $ApiUrl
    Method      = $Method
    Headers     = $headers
    ContentType = "application/json; charset=utf-8"
}

if ($Body) {
    $params.Body = $Body
    Write-Host "   Body   ：$Body" -ForegroundColor Gray
}

Write-Host "`n⏳ 等待响应..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest @params -UseBasicParsing
} catch {
    Write-Host "`n❌ API 调用失败！" -ForegroundColor Red
    Write-Host "HTTP 状态码：$($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    $errorBody = $reader.ReadToEnd()
    
    try {
        $errorJson = $errorBody | ConvertFrom-Json
        Write-Host "错误详情：" -ForegroundColor Red
        Write-Host ($errorJson | ConvertTo-Json -Depth 5) -ForegroundColor Gray
    } catch {
        Write-Host "错误响应：$errorBody" -ForegroundColor Gray
    }
    exit 1
}

# -------------------------------------------------------
# 5. 展示响应
# -------------------------------------------------------
Write-Host "`n✅ API 调用成功！" -ForegroundColor Green
Write-Host ("=" * 60) -ForegroundColor DarkGray
Write-Host "HTTP 状态码：$($response.StatusCode)" -ForegroundColor White

Write-Host ("-" * 60) -ForegroundColor DarkGray
Write-Host "响应头：" -ForegroundColor Cyan
foreach ($key in $response.Headers.Keys) {
    Write-Host "  $key : $($response.Headers[$key])" -ForegroundColor Gray
}

Write-Host ("-" * 60) -ForegroundColor DarkGray
Write-Host "响应体：" -ForegroundColor Cyan
try {
    $responseJson = $response.Content | ConvertFrom-Json
    Write-Host ($responseJson | ConvertTo-Json -Depth 10) -ForegroundColor White
} catch {
    Write-Host $response.Content -ForegroundColor White
}
Write-Host ("=" * 60) -ForegroundColor DarkGray

# -------------------------------------------------------
# 6. 保存响应结果
# -------------------------------------------------------
$resultFile = Join-Path $PSScriptRoot "api_response.json"
$response.Content | Set-Content -Path $resultFile -Encoding UTF8
Write-Host "`n💾 响应已保存至：$resultFile" -ForegroundColor Green
