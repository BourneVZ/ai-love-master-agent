package com.bvz.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileOperationToolTest {

    @Test
    public void testWriteFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "无为而为.txt";
        String content = "万物皆虚，万事皆允；道法自然，顺势而为";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }

    @Test
    public void testReadFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "无为而为.txt";
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }


}
