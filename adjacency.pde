// r is in squares
ArrayList<int[]> getAdjIndices(int x, int y, int r) {
	ArrayList<int[]> adjIndices = new ArrayList<int[]>();
	for(int tx = x-r; tx <= x+r; tx++) {
		for(int ty = y-r; ty <= y+r; ty++) {
			if(tx >= 0 && tx < grid.size() && ty >= 0 && ty < grid.get(0).size()) {
				int[] indices = {tx, ty};
				adjIndices.add(indices);
			}
		}
	}
	return adjIndices;
}

// r is in pixels
ArrayList<int[]> getCircAdjIndices(int x, int y, int r) {
	ArrayList<int[]> adjIndices = getAdjIndices(x, y, r);
	for(int i = adjIndices.size()-1; i >= 0; i--) {
		if((pow(adjIndices.get(i)[0]-x, 2) + pow(adjIndices.get(i)[1]-y, 2)) > pow(r, 2)) {
			adjIndices.remove(i);
		}
	}
	return adjIndices;
}