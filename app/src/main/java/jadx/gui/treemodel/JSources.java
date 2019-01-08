package jadx.gui.treemodel;

import com.ppma.filemanager.R;

import jadx.api.JadxDecompiler;
import jadx.api.JavaPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSources extends JNode {
    private static final long serialVersionUID = 8962924556824862801L;

    private final JadxDecompiler wrapper;

    public JSources(JadxDecompiler wrapper) {
        this.wrapper = wrapper;
        getRootPackage();
    }

    public JSources() {
        this.wrapper = null;
    }

    public List<JPackage> getRootPackage() {
        // build packages hierarchy
        return getHierarchyPackages(wrapper.getPackages());

    }

    /**
     * Convert packages list to hierarchical packages representation
     *
     * @param packages input packages list
     * @return root packages
     */
    public List<JPackage> getHierarchyPackages(List<JavaPackage> packages) {
        Map<String, JPackage> pkgMap = new HashMap<String, JPackage>();
        for (JavaPackage pkg : packages) {
            addPackage(pkgMap, new JPackage(pkg));
        }
        // merge packages without classes
        boolean repeat;
        do {
            repeat = false;
            for (JPackage pkg : pkgMap.values()) {
                if (pkg.getInnerPackages().size() == 1 && pkg.getClasses().isEmpty()) {
                    JPackage innerPkg = pkg.getInnerPackages().get(0);
                    pkg.getInnerPackages().clear();
                    pkg.getInnerPackages().addAll(innerPkg.getInnerPackages());
                    pkg.getClasses().addAll(innerPkg.getClasses());
                    pkg.setName(pkg.getName() + "." + innerPkg.getName());

                    innerPkg.getInnerPackages().clear();
                    innerPkg.getClasses().clear();

                    repeat = true;
                    break;
                }
            }
        } while (repeat);

        // remove empty packages
        for (Iterator<Map.Entry<String, JPackage>> it = pkgMap.entrySet().iterator(); it.hasNext(); ) {
            JPackage pkg = it.next().getValue();
            if (pkg.getInnerPackages().isEmpty() && pkg.getClasses().isEmpty()) {
                it.remove();
            }
        }
        // use identity set for collect inner packages
        Set<JPackage> innerPackages = Collections.newSetFromMap(new IdentityHashMap<JPackage, Boolean>());
        for (JPackage pkg : pkgMap.values()) {
            innerPackages.addAll(pkg.getInnerPackages());
        }
        // find root packages
        List<JPackage> rootPkgs = new ArrayList<JPackage>();
        for (JPackage pkg : pkgMap.values()) {
            if (!innerPackages.contains(pkg)) {
                rootPkgs.add(pkg);
            }
        }
        Collections.sort(rootPkgs);
        return rootPkgs;
    }

    private void addPackage(Map<String, JPackage> pkgs, JPackage pkg) {
        String pkgName = pkg.getName();
        JPackage replaced = pkgs.put(pkgName, pkg);
        if (replaced != null) {
            pkg.getInnerPackages().addAll(replaced.getInnerPackages());
            pkg.getClasses().addAll(replaced.getClasses());
        }
        int dot = pkgName.lastIndexOf('.');
        if (dot > 0) {
            String prevPart = pkgName.substring(0, dot);
            String shortName = pkgName.substring(dot + 1);
            pkg.setName(shortName);
            JPackage prevPkg = pkgs.get(prevPart);
            if (prevPkg == null) {
                prevPkg = new JPackage(prevPart);
                addPackage(pkgs, prevPkg);
            }
            prevPkg.getInnerPackages().add(pkg);
        }
    }

    @Override
    public int getIcon() {
        return R.mipmap.packagefolder_obj;
    }

    @Override
    public JClass getJParent() {
        return null;
    }

    @Override
    public String getName() {
        return "source";
    }

    @Override
    public String makeString() {
        return "Source code";
    }
}
