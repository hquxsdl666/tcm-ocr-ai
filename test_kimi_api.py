#!/usr/bin/env python3
"""
Kimi API 功能测试脚本
用于验证 API Key 和基本功能是否正常
"""

import sys
import requests
import json

# Kimi API 配置
BASE_URL = "https://api.moonshot.cn/v1"

def test_api_key(api_key: str) -> bool:
    """测试 API Key 是否有效"""
    print("=" * 50)
    print("测试1: 验证 API Key")
    print("=" * 50)
    
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    try:
        # 获取模型列表来验证 API Key
        response = requests.get(
            f"{BASE_URL}/models",
            headers=headers,
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ API Key 有效！")
            print(f"   可用模型:")
            for model in data.get("data", []):
                print(f"   - {model.get('id')}")
            return True
        elif response.status_code == 401:
            print(f"❌ API Key 无效 (HTTP 401)")
            print(f"   请检查: ")
            print(f"   1. API Key 是否以 'sk-' 开头")
            print(f"   2. API Key 是否完整（不要遗漏字符）")
            print(f"   3. API Key 是否已过期")
            return False
        else:
            print(f"❌ 请求失败: HTTP {response.status_code}")
            print(f"   响应: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 网络错误: {e}")
        return False

def test_chat(api_key: str) -> bool:
    """测试聊天功能"""
    print("\n" + "=" * 50)
    print("测试2: 测试简单对话")
    print("=" * 50)
    
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": "kimi-latest",
        "messages": [
            {"role": "system", "content": "你是一个 helpful 的助手。"},
            {"role": "user", "content": "你好，请用一句话介绍自己。"}
        ],
        "temperature": 0.7,
        "max_tokens": 100
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat/completions",
            headers=headers,
            json=data,
            timeout=30
        )
        
        if response.status_code == 200:
            result = response.json()
            content = result["choices"][0]["message"]["content"]
            print(f"✅ 对话成功！")
            print(f"   AI回复: {content[:100]}...")
            return True
        else:
            print(f"❌ 对话失败: HTTP {response.status_code}")
            print(f"   响应: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 网络错误: {e}")
        return False

def test_ocr_simulation(api_key: str) -> bool:
    """测试 OCR 功能（模拟）"""
    print("\n" + "=" * 50)
    print("测试3: 测试 OCR 提示词（文本模拟）")
    print("=" * 50)
    
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    ocr_prompt = """你是一个专业的中药药方识别专家。请分析以下药方内容：

药方：当归10g，川芎6g，白芍10g，熟地黄15g

请以JSON格式返回药材信息。"""

    data = {
        "model": "kimi-latest",
        "messages": [
            {"role": "user", "content": ocr_prompt}
        ],
        "temperature": 0.1,
        "max_tokens": 1000,
        "response_format": {"type": "json_object"}
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat/completions",
            headers=headers,
            json=data,
            timeout=30
        )
        
        if response.status_code == 200:
            result = response.json()
            content = result["choices"][0]["message"]["content"]
            print(f"✅ OCR 模拟测试成功！")
            print(f"   返回内容预览: {content[:150]}...")
            return True
        else:
            print(f"❌ OCR 测试失败: HTTP {response.status_code}")
            print(f"   响应: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 网络错误: {e}")
        return False

def main():
    print("\n" + "=" * 50)
    print("Kimi API 功能测试工具")
    print("=" * 50)
    
    # 从命令行参数或输入获取 API Key
    if len(sys.argv) > 1:
        api_key = sys.argv[1]
    else:
        api_key = input("\n请输入您的 Kimi API Key: ").strip()
    
    if not api_key:
        print("❌ API Key 不能为空")
        sys.exit(1)
    
    # 验证 API Key 格式
    if not api_key.startswith("sk-"):
        print("\n⚠️  警告: API Key 不以 'sk-' 开头，这通常不正确")
        print("   Kimi API Key 格式应为: sk-xxxxxxxxxxxxxxxx")
        confirm = input("   是否继续测试? (y/n): ")
        if confirm.lower() != 'y':
            sys.exit(1)
    
    # 运行测试
    results = []
    
    # 测试1: API Key 验证
    results.append(("API Key 验证", test_api_key(api_key)))
    
    # 测试2: 聊天功能
    results.append(("简单对话", test_chat(api_key)))
    
    # 测试3: OCR 模拟
    results.append(("OCR 功能模拟", test_ocr_simulation(api_key)))
    
    # 打印测试总结
    print("\n" + "=" * 50)
    print("测试总结")
    print("=" * 50)
    
    for name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{name}: {status}")
    
    all_passed = all(r[1] for r in results)
    
    if all_passed:
        print("\n🎉 所有测试通过！您的 API Key 可以正常使用。")
        print("\n请在 APP 中输入以下 API Key:")
        print(f"   {api_key}")
    else:
        print("\n⚠️  部分测试失败，请检查以上错误信息。")
    
    return 0 if all_passed else 1

if __name__ == "__main__":
    sys.exit(main())
