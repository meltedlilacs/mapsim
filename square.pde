class Square {
	Square(PVector p0, PVector p1, PVector p2, PVector p3) {
		coords = new PVector[4];
		coords[0] = p0;
		coords[1] = p1;
		coords[2] = p2;
		coords[3] = p3;
		avgX = (coords[0].x+coords[1].x+coords[2].x+coords[3].x)/4;
		avgY = (coords[0].y+coords[1].y+coords[2].y+coords[3].y)/4;
		avgZ = (coords[0].z+coords[1].z+coords[2].z+coords[3].z)/4;
	}

	void draw() {
		fill(c);

		beginShape();
		vertex(coords[0].x, coords[0].y, coords[0].z);
		vertex(coords[1].x, coords[1].y, coords[1].z);
		vertex(coords[2].x, coords[2].y, coords[2].z);
		vertex(coords[3].x, coords[3].y, coords[3].z);
		endShape();
	}

	PVector[] coords;
	Biome biome;
	float avgX, avgY, avgZ;
	color c;
}