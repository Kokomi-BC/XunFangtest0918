"""
============================================================
脚本：call_api.py
功能：使用 X-Auth-Token 调用华为云 API
用法：
    python call_api.py
    python call_api.py --url "https://xxx.myhuaweicloud.com/v1/xxx" --method GET
    python call_api.py --url "https://xxx.myhuaweicloud.com/v1/xxx" --method POST --body '{"key":"value"}'
============================================================
"""

import argparse
import json
import os
import sys
from datetime import datetime, timezone

import requests

# -------------------------------------------------------
# 配置
# -------------------------------------------------------
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TOKEN_FILE = os.path.join(SCRIPT_DIR, "token_response.json")
RESULT_FILE = os.path.join(SCRIPT_DIR, "api_response.json")

# 预设 API 菜单
API_MENU = {
    "1": {
        "name": "IAM - 查询用户列表",
        "url": "https://iam.myhuaweicloud.com/v3/users",
        "method": "GET",
        "description": "GET /v3/users",
    },
    "2": {
        "name": "IAM - 查询账号信息",
        "url": "https://iam.myhuaweicloud.com/v3/auth/domains",
        "method": "GET",
        "description": "GET /v3/auth/domains",
    },
    "3": {
        "name": "ECS - 查询云服务器列表",
        "url_template": "https://ecs.{region}.myhuaweicloud.com/v1/{project_id}/cloudservers",
        "method": "GET",
        "need_region": True,
        "need_project_id": True,
        "description": "GET /v1/{project_id}/cloudservers",
    },
    "4": {
        "name": "VPC - 查询 VPC 列表",
        "url_template": "https://vpc.{region}.myhuaweicloud.com/v1/{project_id}/vpcs",
        "method": "GET",
        "need_region": True,
        "need_project_id": True,
        "description": "GET /v1/{project_id}/vpcs",
    },
}


def load_token() -> dict:
    """加载已保存的 Token 文件。"""
    if not os.path.exists(TOKEN_FILE):
        print(f"❌ 未找到 Token 文件：{TOKEN_FILE}")
        print("请先运行 get_token.py 获取 Token")
        sys.exit(1)

    with open(TOKEN_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def check_token_expiry(token_data: dict) -> None:
    """检查 Token 是否过期并打印剩余有效时间。"""
    expires_at_str = token_data["ExpiresAt"]
    # 解析 ISO 8601 格式，兼容 with/without Z 后缀
    expires_at_str = expires_at_str.replace("Z", "+00:00")
    expires_at = datetime.fromisoformat(expires_at_str)
    now = datetime.now(timezone.utc)

    print("📋 Token 信息：")
    print(f"   用户     ：{token_data['UserName']}")
    print(f"   账号     ：{token_data['DomainName']}")
    print(f"   过期时间 ：{token_data['ExpiresAt']}")

    if now > expires_at:
        print("\n⚠️  Token 已过期！请重新运行 get_token.py 获取新 Token")
        sys.exit(1)

    remaining = expires_at - now
    hours = remaining.total_seconds() // 3600
    minutes = (remaining.total_seconds() % 3600) // 60
    print(f"   剩余有效 ：{int(hours)} 小时 {int(minutes)} 分钟")


def interactive_select() -> tuple:
    """交互式选择要调用的 API，返回 (url, method, body)。"""
    print("\n" + "=" * 60)
    print("  请选择要调用的华为云 API")
    print("=" * 60)
    for key, api in API_MENU.items():
        print(f"  {key}. {api['name']} ({api['description']})")
    print("  5. 自定义 API")
    print("=" * 60)

    choice = input("请输入选项 (1-5): ").strip()

    if choice == "5":
        url = input("请输入完整 API URL: ").strip()
        method = input("请输入 HTTP 方法 (GET/POST/PUT/DELETE): ").strip().upper()
        body_choice = input("是否需要请求体？(y/n): ").strip().lower()
        body = ""
        if body_choice == "y":
            body = input("请输入请求体 JSON: ").strip()
        return url, method, body

    if choice not in API_MENU:
        print("❌ 无效选项")
        sys.exit(1)

    api = API_MENU[choice]

    if api.get("need_region"):
        region = input("请输入 Region（如 cn-south-1）: ").strip()
        url = api["url_template"].replace("{region}", region)
    else:
        url = api["url"]

    if api.get("need_project_id"):
        project_id = input("请输入 Project ID: ").strip()
        url = url.replace("{project_id}", project_id)

    return url, api["method"], ""


def call_api(url: str, method: str, x_auth_token: str, body: str = "") -> dict:
    """发送 API 请求并返回响应。"""
    headers = {
        "X-Auth-Token": x_auth_token,
        "Content-Type": "application/json; charset=utf-8",
    }

    print(f"\n🌐 调用 API：")
    print(f"   URL    ：{method} {url}")
    print(f"   Token  ：X-Auth-Token = {x_auth_token[:30]}...")

    if body:
        print(f"   Body   ：{body}")

    print("\n⏳ 等待响应...")

    try:
        resp = requests.request(
            method=method,
            url=url,
            headers=headers,
            json=json.loads(body) if body else None,
            timeout=30,
        )
    except requests.RequestException as e:
        print(f"\n❌ 网络请求失败：{e}")
        sys.exit(1)
    except json.JSONDecodeError:
        print("\n❌ 请求体 JSON 格式无效")
        sys.exit(1)

    return resp


def print_response(resp: requests.Response) -> None:
    """格式化打印响应内容。"""
    if resp.ok:
        print(f"\n✅ API 调用成功！")
    else:
        print(f"\n❌ API 调用失败！")

    print("=" * 60)
    print(f"HTTP 状态码：{resp.status_code} {resp.reason}")

    print("-" * 60)
    print("响应头：")
    for key, value in resp.headers.items():
        print(f"  {key}: {value}")

    print("-" * 60)
    print("响应体：")
    try:
        response_json = resp.json()
        print(json.dumps(response_json, indent=2, ensure_ascii=False))
    except (ValueError, json.JSONDecodeError):
        print(resp.text)

    print("=" * 60)

    # 保存响应
    with open(RESULT_FILE, "w", encoding="utf-8") as f:
        f.write(resp.text)
    print(f"\n💾 响应已保存至：{RESULT_FILE}")


def main():
    parser = argparse.ArgumentParser(description="使用 X-Auth-Token 调用华为云 API")
    parser.add_argument("--url", "-u", default="", help="API 完整 URL")
    parser.add_argument("--method", "-m", default="GET", help="HTTP 方法 (GET/POST/PUT/DELETE)")
    parser.add_argument("--body", "-b", default="", help="请求体 JSON 字符串")
    args = parser.parse_args()

    # -------------------------------------------------------
    # 1. 加载 Token 并检查有效性
    # -------------------------------------------------------
    token_data = load_token()
    check_token_expiry(token_data)
    x_auth_token = token_data["X_Auth_Token"]

    # -------------------------------------------------------
    # 2. 确定 API 参数（命令行 or 交互式）
    # -------------------------------------------------------
    if args.url:
        url = args.url
        method = args.method.upper()
        body = args.body
    else:
        url, method, body = interactive_select()

    # -------------------------------------------------------
    # 3. 发送请求
    # -------------------------------------------------------
    resp = call_api(url, method, x_auth_token, body)

    # -------------------------------------------------------
    # 4. 展示结果
    # -------------------------------------------------------
    print_response(resp)


if __name__ == "__main__":
    main()
