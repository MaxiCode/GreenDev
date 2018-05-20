public byte[][] gamma (int g, byte[][] x, byte[] gamma){
		
	byte[] gammaByte = gamma;
	byte[] tmp1;
	byte[] tmp2;
	
	_h.update(gammaByte);
	tmp1 = _h.doFinal();
	_h.reset();
	_h.update(tmp1);
	tmp2 = _h.doFinal();
	_h.reset();
	
	transformBytesToLong(tmp1, tmp2);
	
	p=0;
	long j1 = 0;
	long j2 = 0;
	int loopLimit = (int)Math.pow(2, Math.ceil(3.0*g/4.0));

	_hPrime.reset();
	
	for (int i = 0; i < loopLimit; ++i){
		j1 = xorshift1024star() >>> (64 - g);
		j2 = xorshift1024star() >>> (64 - g);
		
		_hPrime.update(helper.concateByteArrays(x[(int)j1], x[(int)j2]));
		x[(int)j1] = _hPrime.doFinal();
		
	}
	return x;
}
