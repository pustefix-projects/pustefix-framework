package org.pustefixframework.util.javascript.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.util.javascript.Compressor;
import org.pustefixframework.util.javascript.CompressorException;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

/**
 * Adapter to the Google Closure Compiler.
 */
public class ClosureCompilerAdapter implements Compressor {

    @Override
    public void compress(Reader reader, Writer writer) throws CompressorException {

        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
        compiler.disableThreads();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        try {
            SourceFile inputFile = SourceFile.fromReader("input.js", reader);
            List<SourceFile> inputFiles = new ArrayList<>();
            inputFiles.add(inputFile);
            List<SourceFile> externFiles = new ArrayList<>();
            compiler.compile(externFiles, inputFiles, options);
            writer.write(compiler.toSource());
        } catch(IOException x) {
            throw new CompressorException("Error while compressing javascript", x);
        }
    }

}
