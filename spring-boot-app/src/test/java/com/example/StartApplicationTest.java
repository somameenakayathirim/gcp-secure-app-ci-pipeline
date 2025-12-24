package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StartApplicationTests {

    @Test
    void contextLoads() {
        // Ensure the Spring context loads correctly
    }

    @Test
    void testIndexPage() {
        // Arrange
        StartApplication app = new StartApplication();
        Model mockModel = mock(Model.class);

        // Act
        String viewName = app.index(mockModel);

        // Assert
        assertEquals("index", viewName, "The returned view name should be 'index'");

        // Verify that the attributes are set on the model
        verify(mockModel).addAttribute("title", "I have successfuly built a sprint boot application using Maven");
        verify(mockModel).addAttribute("msg", "This application is deployed on to Kubernetes using Argo CD");
    }
}
