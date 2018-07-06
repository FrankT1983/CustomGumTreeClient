package GetSourceControlInfo;


import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.github.gumtreediff.gen.srcml.SrcmlCsTreeGenerator;

import java.util.List;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Run.initGenerators();



        if (args.length ==2 )
        {
            CompareFiles(args[0], args[1]);
        }

        if (args.length == 1)
        {
            ParseFile(args[0]);
        }
    }

    private static void ParseFile(String file)
    {
        try
        {
            TreeContext srcContext = Generators.getInstance().getTree(file);
            DumpToCommandline("file", srcContext);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void CompareFiles( String file1, String file2)
    {

        try
        {
            TreeContext srcContext = Generators.getInstance().getTree(file1);
            TreeContext destContext = Generators.getInstance().getTree(file2);
            ITree src = srcContext.getRoot();
            ITree dst = destContext.getRoot();
            Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
            m.match();
            MappingStore mappings = m.getMappings(); // return the mapping store

            ModifiedActionGenerator g = new ModifiedActionGenerator(src, dst, m.getMappings());
            g.generate();
            List<Action> actions = g.getActions(); // return the actions

            DumpToCommandline("source", srcContext);
            DumpToCommandline("destination", destContext);
            DumpToCommandline(actions);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void DumpToCommandline(List<Action> actions)
    {
        for (Action a : actions )
        {
            if (a instanceof ModifiedActionGenerator.ModifiedUpdate)
            {
                System.out.println("Action Modified " + a.getNode().getId() + " "  + ((ModifiedActionGenerator.ModifiedUpdate) a).toNode.getId() );
                continue;
            }

            if (a instanceof Insert)
            {
                System.out.println("Action Insert " + a.getNode().getId() + " " );
                continue;
            }

            if (a instanceof Delete)
            {
                System.out.println("Action Insert " + a.getNode().getId() + " "  );
                continue;
            }

        }
    }

    public static void DumpToCommandline(String prefix ,TreeContext tree)
    {
        List<ITree> bfsDst = TreeUtils.breadthFirst(tree.getRoot());
        for(ITree n : bfsDst)
        {
            System.out.println(prefix+ " n " + n.getId() + " " + n.getPos() + " - " + n.getEndPos() + " " + tree.getTypeLabel(n.getType()));
        }
        DumpEdges( prefix ,tree.getRoot());
    }

    private static void DumpEdges(String prefix, ITree root)
    {
        for (ITree c: root.getChildren())
        {
            System.out.println(prefix+ " e " + root.getId() + " -> " + c.getId());
            DumpEdges(prefix,c);
        }
    }
}
