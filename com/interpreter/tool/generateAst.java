//? Tool to create syntax tree classes.

package com.interpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;



public class generateAst {
    public static void name(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_AST <OUTPUT DIRECTORY>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Unary : Token operator, Expr right"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";

        PrintWriter writer = new PrintWriter(path, "UFT-8");

        writer.println("package com.interpreter.exon;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        //! AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldListString) {
        //! To be continued
    }
}
