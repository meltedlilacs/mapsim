class Tree {
	Tree(float x_, float y_, float z_, float r_, float h_, float snowLength_, float angle_, color c_) {
		x = x_;
		y = y_;
		z = z_;
		r = r_;
		h = h_;
		snowLength = snowLength_;
		s = r*snowLength/h + 1; // +1 to get right outside of the size of the tree
		angle = angle_;
		c = c_;
	}

	void pyramid(float x_, float y_) {
		beginShape();
		vertex(-x_, -x_, 0);
		vertex(0,  0,   y_);
		vertex(x_, -x_, 0);
		endShape();
		beginShape();
		vertex(x_, -x_, 0);
		vertex(0,   0,   y_);
		vertex(x_, x_, 0);
		endShape();
		beginShape();
		vertex(x_, x_, 0);
		vertex(0,   0,   y_);
		vertex(-x_, x_, 0);
		endShape();
		beginShape();
		vertex(-x_, x_, 0);
		vertex(0,   0,   y_);
		vertex(-x_, -x_, 0);
		endShape();
	}

	void draw() {
		fill(c);
		pushMatrix();
		translate(x, y, z);
		rotate(angle);
		pyramid(r, h);
		if(snowLength > 0) {
			fill(colorBiome(Biome.SNOW));
			translate(0, 0, h-snowLength);
			pyramid(s, snowLength);
		}
		popMatrix();
	}

	float x, y, z, r, h, snowLength, s, angle;
	color c;
}