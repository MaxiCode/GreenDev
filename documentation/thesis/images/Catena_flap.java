	private byte[] flap(int g, byte[] xIn, byte[] gamma){
		_hPrime.reset();
		
		byte[] xHinit;
		int iterations = (int)Math.pow(2, g);
		byte[][] v = new byte[iterations+2][_k];
		xHinit = hInit(xIn);
		
		System.arraycopy(xHinit, 0, v[0], 0, _k);
		System.arraycopy(xHinit, _k, v[1], 0, _k);
		
		for (int i=2; i<iterations+2; ++i){
			_hPrime.update(helper.concateByteArrays(v[i-1], v[i-2]));
			v[i] = _hPrime.doFinal();
		}

		byte[][] v2 = new byte[iterations][_k];
		System.arraycopy( v, 2, v2, 0, v2.length );
		
		_hPrime.reset();
		v2 = gamma(g, v2, gamma);
		_hPrime.reset();
		v2 = f(g, v2, _lambda);
		_hPrime.reset();
		v2 = phi(g, v2, v2[v2.length-1]);
		return v2[v2.length-1];
	}