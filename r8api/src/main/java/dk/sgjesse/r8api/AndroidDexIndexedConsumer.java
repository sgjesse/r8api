package dk.sgjesse.r8api;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.utils.ExceptionDiagnostic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * D8/R8 Consumer for DEX programs compatible with Android. This consumer does not use the
 * java.nio.file APIs which are only present on Android from API level 26.
 */
public class AndroidDexIndexedConsumer extends DexIndexedConsumer.ForwardingConsumer {
    private final File destination;
    private final boolean isDirectory;
    private final SortedMap<String, ByteDataView> dexFiles = new TreeMap<>();

    public AndroidDexIndexedConsumer(File destination) {
        super(null);
        this.destination = destination;
        this.isDirectory = destination.isDirectory();
        if (!isDirectory) {
            destination.delete();
        }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
        if (!isDirectory) {
            // Write the DEX files sorted by name.
            try {
                ZipOutputStream stream =
                        new ZipOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(destination)));
                for (Map.Entry<String, ByteDataView> entry : dexFiles.entrySet()) {
                    ZipUtils.writeToZipStream(
                            stream, entry.getKey(), entry.getValue(), ZipEntry.DEFLATED);
                }
                stream.close();
            } catch (IOException e) {
                handler.error(new ExceptionDiagnostic(e));
            }
        }
    }

    @Override
    public void accept(
            int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
        String name = getDefaultDexFileName(fileIndex);
        if (isDirectory) {
            // If writing to a directory write DEX content immediately.
            try {
                new FileOutputStream(name).write(
                        data.getBuffer(), data.getOffset(), data.getLength());
            } catch (IOException e) {
                handler.error(new ExceptionDiagnostic(e));
            }
        } else {
            // Buffer DEX content to ensure deterministic ZIP file. Data is released in the
            // application writer, take a copy.
            dexFiles.put(name, ByteDataView.of(data.copyByteData()));
        }
    }

    // TODO(b/180261430): This method is in DexIndexedConsumer.ForwardingConsumer, but is not kept.
    private String getDefaultDexFileName(int fileIndex) {
        return "classes" + (fileIndex == 0 ? "" : (fileIndex + 1)) + FileUtils.DEX_EXTENSION;
    }
}
