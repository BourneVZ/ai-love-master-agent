package com.bvz.aiagent.agent;

import org.springframework.ai.tool.annotation.Tool;

public class TerminateTool {
  
    @Tool(description = """  
            当任务已经完成，或者当前智能体已经无法继续推进时，调用这个工具结束执行。  
            完成全部工作后必须主动调用该工具，不要继续空转。  
            """)  
    public String doTerminate() {  
        return "任务结束";  
    }  
}
