package jadx.gui.treemodel;

import com.ppma.filemanager.R;

import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.core.dex.info.AccessInfo;


public class JClass extends JNode {
	private static final long serialVersionUID = -1239986875244097177L;

	private final JavaClass cls;
	private final JClass jParent;
	private boolean loaded;

	public JClass(JavaClass cls) {
		this.cls = cls;
		this.jParent = null;
		this.loaded = false;
	}

	public JClass(JavaClass cls, JClass parent) {
		this.cls = cls;
		this.jParent = parent;
		this.loaded = true;
	}

	public JavaClass getCls() {
		return cls;
	}

	public synchronized void load() {
		if (!loaded) {
			cls.decompile();
			loaded = true;
		}
		update();
	}

	public synchronized void update() {
		;
	}


	public String getContent() {
		return cls.getCode();
	}

	@Override
	public int getIcon() {
		AccessInfo accessInfo = cls.getAccessInfo();
		if (accessInfo.isEnum()) {
			return R.mipmap.enum_obj;
		}
		if (accessInfo.isAnnotation()) {
			return R.mipmap.annotation_obj;
		}
		if (accessInfo.isInterface()) {
			return R.mipmap.int_obj;
		}
		if (accessInfo.isProtected()) {
			return R.mipmap.innerclass_protected_obj;
		}
		if (accessInfo.isPrivate()) {
			return R.mipmap.innerclass_private_obj;
		}
		if (accessInfo.isPublic()) {
			return R.mipmap.class_obj;
		}
		return R.mipmap.class_default_obj;
	}

	@Override
	public JavaNode getJavaNode() {
		return cls;
	}

	@Override
	public JClass getJParent() {
		return jParent;
	}

	@Override
	public JClass getRootClass() {
		if (jParent == null) {
			return this;
		}
		return jParent.getRootClass();
	}

	@Override
	public String getName() {
		return cls.getName();
	}

	public String getFullName() {
		return cls.getFullName();
	}

	@Override
	public int getLine() {
		return cls.getDecompiledLine();
	}

	@Override
	public Integer getSourceLine(int line) {
		return cls.getSourceLine(line);
	}

	@Override
	public int hashCode() {
		return cls.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof JClass && cls.equals(((JClass) obj).cls);
	}

	@Override
	public String makeString() {
		return cls.getName();
	}

	@Override
	public String makeLongString() {
		return cls.getFullName();
	}
}
