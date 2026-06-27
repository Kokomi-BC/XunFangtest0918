"""
============================================================
脚本：test_manufacture_api.py
功能：逐个调测 manufacture 模块全部 12 个接口
用法：
    python test_manufacture_api.py              # 交互式菜单
    python test_manufacture_api.py --all        # 全部自动测试
    python test_manufacture_api.py --supplier   # 仅测试供应商
    python test_manufacture_api.py --order      # 仅测试采购订单
============================================================
"""

import json
import os
import sys
import time

import requests

# -------------------------------------------------------
# 配置
# -------------------------------------------------------
BASE_URL = "http://127.0.0.1:8080"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TOKEN_FILE = os.path.join(SCRIPT_DIR, "app_token.json")
RESULT_DIR = os.path.join(SCRIPT_DIR, "test_results")

# 保存测试中创建的 ID，用于后续修改/删除/详情测试
STATE_FILE = os.path.join(SCRIPT_DIR, "test_state.json")


def load_token() -> str:
    """加载已保存的应用 Token。"""
    if not os.path.exists(TOKEN_FILE):
        print(f"❌ 未找到 Token 文件: {TOKEN_FILE}")
        print("   请先运行: python get_app_token.py")
        sys.exit(1)
    with open(TOKEN_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data["token"]


def get_headers(token: str) -> dict:
    return {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    }


def call(method: str, path: str, token: str, body: dict = None) -> dict:
    """统一 HTTP 调用，返回解析后的 JSON。"""
    url = f"{BASE_URL}{path}"
    headers = get_headers(token)
    print(f"\n{'='*60}")
    print(f"  {method} {url}")
    if body:
        print(f"  body: {json.dumps(body, indent=2, ensure_ascii=False)}")

    if method == "GET":
        resp = requests.get(url, headers=headers, params=body)
    elif method == "POST":
        resp = requests.post(url, headers=headers, json=body)
    elif method == "PUT":
        resp = requests.put(url, headers=headers, json=body)
    elif method == "DELETE":
        resp = requests.delete(url, headers=headers)

    print(f"  status: {resp.status_code}")

    try:
        data = resp.json()
    except Exception:
        print(f"  raw: {resp.text[:500]}")
        return None

    # 格式化打印
    pretty = json.dumps(data, indent=2, ensure_ascii=False)
    if len(pretty) > 800:
        pretty = pretty[:800] + "\n  ... (truncated)"
    print(f"  response: {pretty}")
    return data


def load_state() -> dict:
    if os.path.exists(STATE_FILE):
        with open(STATE_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}


def save_state(state: dict):
    os.makedirs(RESULT_DIR, exist_ok=True)
    with open(STATE_FILE, "w", encoding="utf-8") as f:
        json.dump(state, f, indent=2, ensure_ascii=False)


# ==================== 供应商测试 ====================

def test_supplier_add(token: str) -> str:
    """新增供应商 → 返回新创建的 ID"""
    body = {
        "supplierCode": f"SU-TEST-{int(time.time())}",
        "supplierName": "测试供应商-自动化脚本",
        "linkMan": "张三",
        "linkPhone": "13800138000",
        "linkEmail": "test@example.com",
        "supplierType": "1",
        "address": "深圳市南山区科技园",
        "scopeOfSupply": "电子元器件供应",
        "cooperativeStatus": "1",
        "remark": "自动化测试创建的供应商",
    }
    data = call("POST", "/manufacture/supplier", token, body)
    if data and data.get("code") == 200:
        print("  ✅ 新增供应商成功")
        # 返回空（iDME create 不返回 id，需要从列表获取）
        return None
    else:
        print("  ❌ 新增供应商失败")
        return None


def test_supplier_list(token: str) -> list:
    """查询供应商列表 → 返回数据行"""
    data = call("GET", "/manufacture/supplier/list", token,
                {"pageNum": "1", "pageSize": "10"})
    if data and data.get("code") == 200:
        rows = data.get("rows", [])
        print(f"  ✅ 查询列表成功，共 {data.get('total', 0)} 条，当前页 {len(rows)} 条")
        return rows
    else:
        print("  ❌ 查询列表失败")
        return []


def test_supplier_detail(token: str, supplier_id: str):
    """查询供应商详情"""
    data = call("GET", f"/manufacture/supplier/{supplier_id}", token)
    if data and data.get("code") == 200:
        print("  ✅ 查询详情成功")
    else:
        print("  ❌ 查询详情失败")


def test_supplier_update(token: str, supplier_id: str):
    """修改供应商"""
    body = {
        "id": supplier_id,
        "supplierCode": f"SU-TEST-{int(time.time())}",
        "supplierName": "测试供应商-已修改",
        "linkMan": "李四",
        "linkPhone": "13900139000",
        "linkEmail": "updated@example.com",
        "supplierType": "2",
        "address": "广州市黄埔区",
        "scopeOfSupply": "机械设备供应",
        "cooperativeStatus": "2",
        "remark": "自动化测试-已修改",
    }
    data = call("PUT", "/manufacture/supplier", token, body)
    if data and data.get("code") == 200:
        print("  ✅ 修改供应商成功")
    else:
        print("  ❌ 修改供应商失败")


def test_supplier_delete_one(token: str, supplier_id: str):
    """删除单个供应商"""
    data = call("DELETE", f"/manufacture/supplier/delete/{supplier_id}", token)
    if data and data.get("code") == 200:
        print("  ✅ 删除成功")
    else:
        print("  ⚠️ 删除返回非200（可能已删除或权限问题）")


def test_supplier_delete_batch(token: str, ids: list):
    """批量删除供应商"""
    if not ids:
        print("  ⚠️ 无可删除的ID")
        return
    path = "/manufacture/supplier/" + ",".join(ids)
    data = call("DELETE", path, token)
    if data and data.get("code") == 200:
        print(f"  ✅ 批量删除 {len(ids)} 条成功")
    else:
        print("  ⚠️ 批量删除返回非200")


# ==================== 采购订单测试 ====================

def test_order_add(token: str):
    """新增采购订单"""
    body = {
        "purchaseOrderCode": f"PO-TEST-{int(time.time())}",
        "purchaseDate": "2025-09-16",
        "supplierName": "测试供应商",
        "supplierLinkMan": "王五",
        "materialCode": "MAT-001",
        "materialName": "测试物料",
        "specificationsModels": "规格A-2025",
        "purchaseQuantity": "100",
        "unit": "个",
        "unitPrice": "50.00",
        "totalPrice": "5000.00",
        "status": "1",
        "remark": "自动化测试创建的采购订单",
    }
    data = call("POST", "/manufacture/order", token, body)
    if data and data.get("code") == 200:
        print("  ✅ 新增采购订单成功")
    else:
        print("  ❌ 新增采购订单失败")


def test_order_list(token: str) -> list:
    """查询采购订单列表"""
    data = call("GET", "/manufacture/order/list", token,
                {"pageNum": "1", "pageSize": "10"})
    if data and data.get("code") == 200:
        rows = data.get("rows", [])
        print(f"  ✅ 查询列表成功，共 {data.get('total', 0)} 条，当前页 {len(rows)} 条")
        return rows
    else:
        print("  ❌ 查询列表失败")
        return []


def test_order_list_with_filter(token: str):
    """带筛选条件的列表查询"""
    body = {
        "pageNum": "1",
        "pageSize": "10",
        "status": "1",
        "purchaseDateStart": "2025-01-01",
        "purchaseDateEnd": "2025-12-31",
    }
    data = call("GET", "/manufacture/order/list", token, body)
    if data and data.get("code") == 200:
        print(f"  ✅ 带筛选查询成功，共 {data.get('total', 0)} 条")
    else:
        print("  ❌ 带筛选查询失败")


def test_order_detail(token: str, order_id: str):
    """查询采购订单详情"""
    data = call("GET", f"/manufacture/order/{order_id}", token)
    if data and data.get("code") == 200:
        print("  ✅ 查询详情成功")
    else:
        print("  ❌ 查询详情失败")


def test_order_update(token: str, order_id: str):
    """修改采购订单"""
    body = {
        "id": order_id,
        "purchaseOrderCode": f"PO-TEST-{int(time.time())}",
        "purchaseDate": "2025-10-01",
        "supplierName": "测试供应商-已修改",
        "supplierLinkMan": "赵六",
        "materialCode": "MAT-002",
        "materialName": "测试物料-已修改",
        "specificationsModels": "规格B-2025",
        "purchaseQuantity": "200",
        "unit": "箱",
        "unitPrice": "60.00",
        "totalPrice": "12000.00",
        "status": "2",
        "remark": "自动化测试-已修改",
    }
    data = call("PUT", "/manufacture/order", token, body)
    if data and data.get("code") == 200:
        print("  ✅ 修改采购订单成功")
    else:
        print("  ❌ 修改采购订单失败")


def test_order_delete_one(token: str, order_id: str):
    """删除单个采购订单"""
    data = call("DELETE", f"/manufacture/order/delete/{order_id}", token)
    if data and data.get("code") == 200:
        print("  ✅ 删除成功")
    else:
        print("  ⚠️ 删除返回非200")


def test_order_delete_batch(token: str, ids: list):
    """批量删除采购订单"""
    if not ids:
        print("  ⚠️ 无可删除的ID")
        return
    path = "/manufacture/order/" + ",".join(ids)
    data = call("DELETE", path, token)
    if data and data.get("code") == 200:
        print(f"  ✅ 批量删除 {len(ids)} 条成功")
    else:
        print("  ⚠️ 批量删除返回非200")


# ==================== 菜单 ====================

def menu_supplier(token: str):
    """供应商模块交互菜单"""
    state = load_state()
    supplier_ids = state.get("supplier_ids", [])

    while True:
        print(f"\n{'='*40}")
        print("  🏭 供应商模块测试菜单")
        print(f"  已缓存 ID: {supplier_ids}")
        print(f"  1. 新增供应商")
        print(f"  2. 查询列表")
        print(f"  3. 查询详情（需先有ID）")
        print(f"  4. 修改供应商（需先有ID）")
        print(f"  5. 删除单个（需先有ID）")
        print(f"  6. 批量删除（需先有ID）")
        print(f"  7. 一键全流程（增→查→改→删）")
        print(f"  0. 返回主菜单")

        choice = input("  请选择: ").strip()

        if choice == "1":
            test_supplier_add(token)
            rows = test_supplier_list(token)
            if rows:
                supplier_ids = [r.get("id") for r in rows if r.get("id")]
                state["supplier_ids"] = supplier_ids
                save_state(state)
        elif choice == "2":
            rows = test_supplier_list(token)
            if rows:
                supplier_ids = [r.get("id") for r in rows if r.get("id")]
                state["supplier_ids"] = supplier_ids
                save_state(state)
        elif choice == "3":
            sid = _pick_id(supplier_ids, "供应商")
            if sid: test_supplier_detail(token, sid)
        elif choice == "4":
            sid = _pick_id(supplier_ids, "供应商")
            if sid: test_supplier_update(token, sid)
        elif choice == "5":
            sid = _pick_id(supplier_ids, "供应商")
            if sid: test_supplier_delete_one(token, sid)
        elif choice == "6":
            test_supplier_delete_batch(token, supplier_ids[:3])
        elif choice == "7":
            print("\n--- 全流程开始 ---")
            test_supplier_add(token)
            rows = test_supplier_list(token)
            supplier_ids = [r.get("id") for r in rows if r.get("id")]
            state["supplier_ids"] = supplier_ids
            save_state(state)
            if supplier_ids:
                sid = supplier_ids[0]
                test_supplier_detail(token, sid)
                test_supplier_update(token, sid)
                test_supplier_delete_one(token, sid)
            print("--- 全流程结束 ---")
        elif choice == "0":
            break


def menu_order(token: str):
    """采购订单模块交互菜单"""
    state = load_state()
    order_ids = state.get("order_ids", [])

    while True:
        print(f"\n{'='*40}")
        print("  📦 采购订单模块测试菜单")
        print(f"  已缓存 ID: {order_ids}")
        print(f"  1. 新增采购订单")
        print(f"  2. 查询列表")
        print(f"  3. 带筛选查询列表（日期+状态）")
        print(f"  4. 查询详情（需先有ID）")
        print(f"  5. 修改采购订单（需先有ID）")
        print(f"  6. 删除单个（需先有ID）")
        print(f"  7. 批量删除（需先有ID）")
        print(f"  8. 一键全流程（增→查→改→删）")
        print(f"  0. 返回主菜单")

        choice = input("  请选择: ").strip()

        if choice == "1":
            test_order_add(token)
            rows = test_order_list(token)
            if rows:
                order_ids = [r.get("id") for r in rows if r.get("id")]
                state["order_ids"] = order_ids
                save_state(state)
        elif choice == "2":
            rows = test_order_list(token)
            if rows:
                order_ids = [r.get("id") for r in rows if r.get("id")]
                state["order_ids"] = order_ids
                save_state(state)
        elif choice == "3":
            test_order_list_with_filter(token)
        elif choice == "4":
            oid = _pick_id(order_ids, "采购订单")
            if oid: test_order_detail(token, oid)
        elif choice == "5":
            oid = _pick_id(order_ids, "采购订单")
            if oid: test_order_update(token, oid)
        elif choice == "6":
            oid = _pick_id(order_ids, "采购订单")
            if oid: test_order_delete_one(token, oid)
        elif choice == "7":
            test_order_delete_batch(token, order_ids[:3])
        elif choice == "8":
            print("\n--- 全流程开始 ---")
            test_order_add(token)
            rows = test_order_list(token)
            order_ids = [r.get("id") for r in rows if r.get("id")]
            state["order_ids"] = order_ids
            save_state(state)
            if order_ids:
                oid = order_ids[0]
                test_order_detail(token, oid)
                test_order_update(token, oid)
                test_order_delete_one(token, oid)
            print("--- 全流程结束 ---")
        elif choice == "0":
            break


def _pick_id(id_list: list, label: str) -> str:
    """从缓存 ID 列表中选一个。"""
    if not id_list:
        print(f"  ⚠️ 没有可用的{label}ID，请先查询列表")
        return None
    print(f"  可用{label}ID:")
    for i, sid in enumerate(id_list[:10]):
        print(f"    [{i}] {sid}")
    sel = input(f"  选择索引 (默认0): ").strip()
    try:
        idx = int(sel) if sel else 0
        return id_list[idx]
    except (ValueError, IndexError):
        return id_list[0]


# ==================== 主入口 ====================

def print_banner():
    print("=" * 60)
    print("  🔧 manufacture 模块 API 调测工具")
    print(f"  目标: {BASE_URL}")
    print("=" * 60)


def main():
    print_banner()

    # 加载 Token
    try:
        token = load_token()
        print("✅ Token 已加载")
    except SystemExit:
        return

    # 参数模式
    if len(sys.argv) > 1:
        arg = sys.argv[1]
        if arg == "--all":
            print("\n--- 供应商全流程 ---")
            test_supplier_add(token)
            rows = test_supplier_list(token)
            state = {"supplier_ids": [r.get("id") for r in rows if r.get("id")]}
            save_state(state)
            if state["supplier_ids"]:
                sid = state["supplier_ids"][0]
                test_supplier_detail(token, sid)
                test_supplier_update(token, sid)
                test_supplier_delete_one(token, sid)

            print("\n--- 采购订单全流程 ---")
            test_order_add(token)
            rows = test_order_list(token)
            state["order_ids"] = [r.get("id") for r in rows if r.get("id")]
            save_state(state)
            if state.get("order_ids"):
                oid = state["order_ids"][0]
                test_order_detail(token, oid)
                test_order_update(token, oid)
                test_order_delete_one(token, oid)
            return
        elif arg == "--supplier":
            menu_supplier(token)
            return
        elif arg == "--order":
            menu_order(token)
            return

    # 交互主菜单
    while True:
        print(f"\n{'='*40}")
        print("  主菜单")
        print("  1. 供应商模块测试")
        print("  2. 采购订单模块测试")
        print("  3. 一键全流程测试（增→查→改→删）")
        print("  0. 退出")

        choice = input("  请选择: ").strip()

        if choice == "1":
            menu_supplier(token)
        elif choice == "2":
            menu_order(token)
        elif choice == "3":
            print("\n--- 供应商全流程 ---")
            test_supplier_add(token)
            rows_s = test_supplier_list(token)
            sids = [r.get("id") for r in rows_s if r.get("id")]
            save_state({"supplier_ids": sids, "order_ids": []})
            if sids:
                test_supplier_detail(token, sids[0])
                test_supplier_update(token, sids[0])
                test_supplier_delete_one(token, sids[0])

            print("\n--- 采购订单全流程 ---")
            test_order_add(token)
            rows_o = test_order_list(token)
            oids = [r.get("id") for r in rows_o if r.get("id")]
            save_state({"supplier_ids": sids, "order_ids": oids})
            if oids:
                test_order_detail(token, oids[0])
                test_order_update(token, oids[0])
                test_order_delete_one(token, oids[0])
        elif choice == "0":
            print("👋 退出")
            break


if __name__ == "__main__":
    main()
