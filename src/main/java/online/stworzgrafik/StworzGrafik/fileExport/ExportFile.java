package online.stworzgrafik.StworzGrafik.fileExport;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;

import java.io.IOException;

public interface ExportFile {
    byte[] export(ScheduleGeneratorContext context) throws IOException;
}
