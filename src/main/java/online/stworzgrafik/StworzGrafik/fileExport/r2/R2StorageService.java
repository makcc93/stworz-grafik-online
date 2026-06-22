package online.stworzgrafik.StworzGrafik.fileExport.r2;

public interface R2StorageService {
    String uploadAndPresign(byte[] data, String key, String contentType);
}
