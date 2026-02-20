package com.tcm.app.utils

object Constants {
    // Kimi API (Moonshot) 配置
    const val KIMI_BASE_URL = "https://api.moonshot.cn/"
    const val KIMI_MODEL = "kimi-latest"
    
    // 兼容旧代码的别名
    const val DEEPSEEK_BASE_URL = KIMI_BASE_URL
    const val DEEPSEEK_MODEL = KIMI_MODEL
    
    // OCR Prompt
    const val OCR_PROMPT = """你是一个专业的中药药方识别专家。请仔细识别这张药方图片，提取以下信息：

1. **药材列表**：每种药材的名称、剂量（如g、钱、两）、炮制方法（如炙、炒、生）
2. **用法用量**：煎煮方法、服用频次、每次用量
3. **方剂信息**：方剂名称（如有）、功效主治、适用症状
4. **特殊标记**：先煎、后下、包煎等特殊煎煮要求

请以以下JSON格式返回（不要添加任何其他说明文字，确保返回的是合法JSON）：
{
  "prescription_name": "方剂名称，未知则为空",
  "herbs": [
    {
      "name": "药材名",
      "dosage": "剂量",
      "preparation": "炮制方法"
    }
  ],
  "usage": {
    "decoction": "煎煮方法",
    "frequency": "服用频次",
    "dosage_per_time": "每次用量"
  },
  "indications": "主治功效",
  "special_notes": "特殊煎煮要求",
  "confidence": 0.95
}"""

    // AI System Prompt
    const val AI_SYSTEM_PROMPT = """你是一位经验丰富的中医医师，精通中医经典方剂。你的职责是：

1. **方剂解析**：解释方剂的组成原理、君臣佐使配伍关系
2. **病症分析**：根据患者症状，分析病因病机
3. **方剂推荐**：基于用户的历史方剂库，推荐合适的治疗方案
4. **用药指导**：提供专业的用药建议和注意事项

回答时请：
- 使用专业但易懂的语言
- 结合传统中医理论
- 注明仅供参考，建议咨询专业医师
- 如有不确定的地方，请明确说明"""

    const val DATABASE_NAME = "tcm_database"
    const val PREFS_NAME = "tcm_prefs"
    const val PREFS_API_KEY = "deepseek_api_key"
}
