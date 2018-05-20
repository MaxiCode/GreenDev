public byte[][] graph(int g, byte[][] v, int lambda) {
		
	int dim1 = (int)Math.pow(2, g);;
	int dim2 = _hPrime.getOutputSize();
	byte [][] r = new byte[dim1][dim2];
	
	int jLimit = 2*g - 1;
	int iLimit = dim1-1;
	
	_hPrime.reset();
	
	for (int k = 1; k <= lambda; k++) {
		for (int j = 1; j <= jLimit; j++) {

			r[0] = hFirst(helper.concateByteArrays(
				helper.xor(v[dim1-1],v[0]),
				v[(int)indexing.getIndex((long)g, (long)j-1, 0)]));
			
			for (int i = 1; i <= iLimit; i++) {
				_hPrime.update(helper.concateByteArrays(
					helper.xor(r[i-1],v[i]),
					v[(int)indexing.getIndex((long)g, (long)j-1, i)]));
				r[i] = _hPrime.doFinal();
			}
			System.arraycopy(r, 0, v, 0, r.length);
		}
	}
	return v;
}
