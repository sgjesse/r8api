package dk.sgjesse.d8onandroid;

import android.os.Bundle;

import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.io.ByteStreams;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dk.sgjesse.r8api.ArchiveProgramResourceProvider;
import dk.sgjesse.r8api.AndroidDexIndexedConsumer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", this::onCompile).show();
            }

            private void onCompile(View view) {
            }
        });

        try {
            String tmpDir = getApplicationContext().getCacheDir().getPath();
            String helloClassFileName = tmpDir + "/program.jar";
            OutputStream out = new FileOutputStream(helloClassFileName);
            /* Simple program for testing:

                interface I {
            	    default void m() {
            		    System.out.println("Hello, world!");
            	    }
                }

                public class A implements I {
            	    public static void main(String[] args) {
            		    new A().m();
            	    }
                }

              program.jar has both classes program_without_interface.jar only has class A.
            */

            if (true) {
                ByteStreams.copy(getAssets().open("program.jar"), out);
            } else {
                ByteStreams.copy(getAssets().open("program_without_interface.jar"), out);
            }
            File outputArchive = new File(tmpDir + "/out.zip");
            D8.run(
                    D8Command.builder(new D8DiagnosticsHandler())
                            .addProgramResourceProvider(
                                    new ArchiveProgramResourceProvider(new File(helloClassFileName)))
                            .setProgramConsumer(new AndroidDexIndexedConsumer(outputArchive))
                            .build());

            ClassLoader classLoader =
                    new DexClassLoader(
                            outputArchive.toString(), tmpDir, null, this.getClassLoader());
            Class<?> aClass = classLoader.loadClass("A");
            Method main = aClass.getMethod("main", String[].class);
            main.invoke(null, new Object[]{new String[]{}});


        } catch (Throwable t) {
            System.out.println("FAILED, " +t.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}