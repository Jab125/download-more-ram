package dev.jab125.drm;

public final class DisplaySection implements Cloneable {
	public float xD;
	public float yD;
	public float xW;
	public float yW;
	public DisplaySection(float xD, float yD, float xW, float yW) {
		this.xD = xD;
		this.yD = yD;
		this.xW = xW;
		this.yW = yW;
	}

	@Override
	public DisplaySection clone()  {
		try {
			return (DisplaySection) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
