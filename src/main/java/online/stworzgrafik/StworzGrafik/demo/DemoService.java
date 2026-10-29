package online.stworzgrafik.StworzGrafik.demo;

import online.stworzgrafik.StworzGrafik.demo.DTO.DemoResponse;

public interface DemoService {
    DemoResponse createDemo();
    void deleteDemo(Long userId, Long storeId);
}
