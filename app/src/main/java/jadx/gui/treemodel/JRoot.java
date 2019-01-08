package jadx.gui.treemodel;

import com.ppma.filemanager.R;

import jadx.api.JadxDecompiler;
import jadx.api.ResourceFile;
import jadx.gui.JadxWrapper;
import jadx.gui.treemodel.JResource.JResType;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class JRoot extends JNode {
    private static final long serialVersionUID = 8888495789773527342L;

    private static final int ROOT_ICON = R.mipmap.java_model_obj;

    private final JadxDecompiler wrapper;

    private boolean flatPackages = false;

    private JResource resource;

    private JSources sources;

    public JRoot(JadxDecompiler wrapper) {
        this.wrapper = wrapper;
        sources = new JSources(wrapper);
        resource = new JResource(null, "Resources", JResType.ROOT);
    }


    public JSources getSources() {
        return sources;
    }

    public JResource getResource() {
        return resource;
    }


    private List<JResource> getHierarchyResources(List<ResourceFile> resources) {
        if (resources.isEmpty()) {
            return Collections.emptyList();
        }
        JResource root = new JResource(null, "Resources", JResType.ROOT);
        String splitPathStr = Pattern.quote(File.separator);
        for (ResourceFile rf : resources) {
            String[] parts = new File(rf.getName()).getPath().split(splitPathStr);
            JResource curRf = root;
            int count = parts.length;
            for (int i = 0; i < count; i++) {
                String name = parts[i];
                JResource subRF = getResourceByName(curRf, name);
                if (subRF == null) {
                    if (i != count - 1) {
                        subRF = new JResource(null, name, JResType.DIR);
                    } else {
                        subRF = new JResource(rf, name, JResType.FILE);
                    }
                    curRf.getFiles().add(subRF);
                }
                curRf = subRF;
            }
        }
        return Collections.singletonList(root);
    }

    private JResource getResourceByName(JResource rf, String name) {
        for (JResource sub : rf.getFiles()) {
            if (sub.getName().equals(name)) {
                return sub;
            }
        }
        return null;
    }


    public boolean isFlatPackages() {
        return flatPackages;
    }

    public void setFlatPackages(boolean flatPackages) {
        if (this.flatPackages != flatPackages) {
            this.flatPackages = flatPackages;
        }
    }

    @Override
    public int getIcon() {
        return ROOT_ICON;
    }

    @Override
    public JClass getJParent() {
        return null;
    }

    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public String makeString() {
        return null;
    }
}
