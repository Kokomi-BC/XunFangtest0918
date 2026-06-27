"""
============================================================
脚本：get_app_token.py
功能：自动获取 Spring Boot 应用 JWT Token
流程：GET /code → 手动输入验证码 → POST /auth/login → 保存 Token
============================================================
"""

import base64
import json
import os
import sys

import requests

# -------------------------------------------------------
# 配置
# -------------------------------------------------------
BASE_URL = "http://127.0.0.1:8080"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TOKEN_FILE = os.path.join(SCRIPT_DIR, "app_token.json")

# 登录凭据（按实际修改）
USERNAME = "admin"
PASSWORD = "admin123"


def get_code():
    """调用 /code 获取 uuid 和验证码图片（Base64）。"""
    url = f"{BASE_URL}/code"
    print(f"[1/3] GET {url} ...")
    resp = requests.get(url)
    if resp.status_code != 200:
        print(f"❌ 获取验证码失败: HTTP {resp.status_code}")
        sys.exit(1)

    data = resp.json()
    uuid = data.get("uuid")
    img_base64 = data.get("img")

    if not uuid or not img_base64:
        print("❌ 响应缺少 uuid 或 img 字段")
        print(json.dumps(data, indent=2, ensure_ascii=False))
        sys.exit(1)

    print(f"  ✅ uuid = {uuid}")

    # 保存验证码图片到本地
    img_path = os.path.join(SCRIPT_DIR, "captcha.png")
    with open(img_path, "wb") as f:
        f.write(base64.b64decode(img_base64))
    print(f"  📸 验证码已保存到: {img_path}")
    print(f"  💡 请打开 {img_path} 查看验证码（可用浏览器或图片查看器）")

    return uuid


def login(uuid: str, code: str):
    """用验证码登录，获取 Token。"""
    url = f"{BASE_URL}/auth/login"
    body = {
        "username": USERNAME,
        "password": PASSWORD,
        "code": code,
        "uuid": uuid,
    }
    print(f"\n[2/3] POST {url}")
    print(f"  body = {json.dumps(body, ensure_ascii=False)}")

    resp = requests.post(url, json=body)
    if resp.status_code != 200:
        print(f"❌ 登录失败: HTTP {resp.status_code}")
        print(f"  response: {resp.text}")
        sys.exit(1)

    data = resp.json()
    token = data.get("token")
    if not token:
        print("❌ 响应中未找到 token 字段")
        print(json.dumps(data, indent=2, ensure_ascii=False))
        sys.exit(1)

    print(f"  ✅ Token 获取成功")
    print(f"  Token (前80字符): {token[:80]}...")
    return token


def save_token(token: str):
    """保存 Token 到本地文件。"""
    print(f"\n[3/3] 保存 Token 到 {TOKEN_FILE}")
    with open(TOKEN_FILE, "w", encoding="utf-8") as f:
        json.dump({"token": token}, f, ensure_ascii=False)
    print("  ✅ 已保存")


def main():
    print("=" * 60)
    print("  应用 JWT Token 获取工具")
    print(f"  目标: {BASE_URL}")
    print("=" * 60)

    # Step 1: 获取验证码
    uuid = get_code()

    # Step 2: 手动输入验证码
    print()
    code = input("🔤 请输入验证码（4位数字/字母）: ").strip()
    if not code:
        print("❌ 验证码不能为空")
        sys.exit(1)

    # Step 3: 登录获取 Token
    token = login(uuid, code)

    # Step 4: 保存
    save_token(token)

    print("\n" + "=" * 60)
    print("  🎉 Token 就绪！现在可以运行 test_manufacture_api.py 调试接口")
    print("=" * 60)


if __name__ == "__main__":
    main()
