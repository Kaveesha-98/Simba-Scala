import scala.math.pow

def Multiplexer(ORDER: Int, WIDTH: Int, IN: chisel3.UInt, SELECT: chisel3.UInt): chisel3.UInt = {
	
	val wordVector = Wire(Vec(UInt(WIDTH.W), pow(2, WIDTH)))
	
	for(i <- 0 to pow(2, WIDTH)){
		wordVector(i) := IN((i+1)*WIDTH - 1, i*WIDTH)
	}
	
	wordVector(SELECT)

}


val OUT = Multiplexer(5, 8, cacheLine, sel)
