"""
============================================================
脚本：get_token.py
功能：调用华为云 IAM 接口获取 X-Subject-Token
     并将其重命名为 X-Auth-Token 保存到本地文件
============================================================
"""

import json
import os
import re
import sys
from datetime import datetime

import requests

# -------------------------------------------------------
# 1. 读取认证配置文件（支持 JSON 中的 // 行内注释）
# -------------------------------------------------------
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_PATH = os.path.join(SCRIPT_DIR, "token获取")
TOKEN_OUTPUT_PATH = os.path.join(SCRIPT_DIR, "token_response.json")


def load_json_with_comments(filepath: str) -> dict:
    """读取包含 // 注释的 JSON 文件，自动去除注释后解析。"""
    with open(filepath, "r", encoding="utf-8") as f:
        raw = f.read()

    # 去除 // 开头的行内注释（不处理字符串内的 //）
    cleaned = re.sub(r"\s*//.*$", "", raw, flags=re.MULTILINE)

    return json.loads(cleaned)


def main():
    print(f"📖 读取认证配置：{CONFIG_PATH}")
    auth_config = load_json_with_comments(CONFIG_PATH)

    # -------------------------------------------------------
    # 2. 构建完整请求体（补充 scope 字段）
    # -------------------------------------------------------
    identity = auth_config["auth"]["identity"]
    domain_name = identity["password"]["user"]["domain"]["name"]

    request_body = {
        "auth": {
            "identity": identity,
            "scope": {
                "domain": {
                    "name": domain_name
                }
            }
        }
    }

    print("📤 请求体：")
    print(json.dumps(request_body, indent=2, ensure_ascii=False))

    # -------------------------------------------------------
    # 3. 发送 POST 请求获取 Token
    # -------------------------------------------------------
    iam_url = "https://iam.myhuaweicloud.com/v3/auth/tokens"
    print(f"\n🌐 发送请求：POST {iam_url}")

    try:
        resp = requests.post(
            iam_url,
            json=request_body,
            headers={"Content-Type": "application/json; charset=utf-8"},
            timeout=30,
        )
    except requests.RequestException as e:
        print(f"\n❌ 网络请求失败：{e}")
        sys.exit(1)

    # -------------------------------------------------------
    # 4. 检查 HTTP 状态码
    # -------------------------------------------------------
    if resp.status_code != 201:
        print(f"\n❌ 请求失败！")
        print(f"HTTP 状态码：{resp.status_code}")
        try:
            error_body = resp.json()
            print(f"错误详情：\n{json.dumps(error_body, indent=2, ensure_ascii=False)}")
        except ValueError:
            print(f"错误响应：{resp.text}")
        sys.exit(1)

    # -------------------------------------------------------
    # 5. 提取 X-Subject-Token（响应头）
    # -------------------------------------------------------
    subject_token = resp.headers.get("X-Subject-Token")

    if not subject_token:
        print("\n❌ 响应头中未找到 X-Subject-Token！")
        print("响应头列表：")
        for key, value in resp.headers.items():
            print(f"  {key} = {value}")
        sys.exit(1)

    # -------------------------------------------------------
    # 6. 解析响应体
    # -------------------------------------------------------
    token_info = resp.json()["token"]

    print("\n✅ Token 获取成功！")
    print("=" * 60)
    print(f"X-Subject-Token (前60字符)：{subject_token[:60]}")
    print(f"Token 完整值长度：{len(subject_token)} 字符")
    print("-" * 60)
    print(f"签发时间   ：{token_info['issued_at']}")
    print(f"过期时间   ：{token_info['expires_at']}")
    print(f"用户名     ：{token_info['user']['name']}")
    print(f"所属账号   ：{token_info['user']['domain']['name']}")
    print(f"用户 ID    ：{token_info['user']['id']}")
    print("-" * 60)
    print("服务目录   ：")
    for catalog in token_info.get("catalog", []):
        print(f"  📦 {catalog['name']} ({catalog['type']})")
        for ep in catalog.get("endpoints", []):
            print(f"      → {ep.get('region_id', 'N/A')}: {ep['url']}")
    print("=" * 60)

    # -------------------------------------------------------
    # 7. 保存 Token 到文件（供 call_api.py 使用）
    # -------------------------------------------------------
    token_data = {
        "X_Subject_Token": subject_token,
        "X_Auth_Token": subject_token,        # 重命名后的 Token
        "IssuedAt": token_info["issued_at"],
        "ExpiresAt": token_info["expires_at"],
        "UserName": token_info["user"]["name"],
        "DomainName": token_info["user"]["domain"]["name"],
    }

    with open(TOKEN_OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(token_data, f, indent=2, ensure_ascii=False)

    print(f"\n💾 Token 已保存至：{TOKEN_OUTPUT_PATH}")
    print("\n📌 提示：后续 API 调用时请使用 X-Auth-Token 作为请求头名称")


if __name__ == "__main__":
    main()
