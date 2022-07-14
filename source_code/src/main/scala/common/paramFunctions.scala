import chisel3._
import chisel3.util._
import chisel3.Driver

object paramFunctions {

    /**
     * function used map a hardware wire to a output
     *
     * @param mapEntries Seq[(inputMatchCase, result)]
     * @param default output when none of the inputMatchCase matches
     * @param f Matching function(function that compares inputMatchCase and mapInput)
     * @param mapInput input to hardware implemented map
     * @return TODO(Kaveesha)
     */
    def mapInputToOutput[T <: Data](mapEntries: Seq[(T, T)], default: T, f: (T, T) => chisel3.Bool)( mapInput: T): T = {
        val conditionArray = Seq.tabulate(mapEntries.length)(i => {
            mapEntries(i) match {
                case (matchDataCase, matchResult) => f(matchDataCase, mapInput) -> matchResult
            }
        })

        MuxCase(default, conditionArray)
    }

    val typeSeq = Seq(pipelineParams.rtype, pipelineParams.itype, 
                      pipelineParams.stype, pipelineParams.btype, 
                      pipelineParams.utype, pipelineParams.jtype, 
                      pipelineParams.ntype) 

    //opcode -> type_w
    val instructionOpcodeToTypeMap = Seq((pipelineParams.lui.U,     pipelineParams.utype.U), 
                                         (pipelineParams.auipc.U,   pipelineParams.utype.U), 
                                         (pipelineParams.jump.U,    pipelineParams.jtype.U), 
                                         (pipelineParams.jumpr.U,   pipelineParams.itype.U), 
                                         (pipelineParams.cjump.U,   pipelineParams.btype.U), 
                                         (pipelineParams.load.U,    pipelineParams.itype.U), 
                                         (pipelineParams.store.U,   pipelineParams.stype.U), 
                                         (pipelineParams.iops.U,    pipelineParams.itype.U), 
                                         (pipelineParams.iops32.U,  pipelineParams.itype.U), 
                                         (pipelineParams.rops.U,    pipelineParams.rtype.U), 
                                         (pipelineParams.rops32.U,  pipelineParams.rtype.U), 
                                         (pipelineParams.system.U,  pipelineParams.itype.U), 
                                         (pipelineParams.fence.U,   pipelineParams.ntype.U), 
                                         (pipelineParams.amos.U,    pipelineParams.rtype.U))
	//default :   TYPE=ntype;
    val INS_TYPE_ROM = mapInputToOutput(instructionOpcodeToTypeMap, pipelineParams.ntype.U, (x: chisel3.UInt, y: chisel3.UInt) => x === y)(_)

    /*for (x, 32) -> repeat instruction(31) x times
          (x, 0) -> repeat 1'b0 x times
          (x, y) -> instruction(x, y)
    */
    val itype_imm = Seq((53, 32), (30, 20))
    val stype_imm = Seq((53, 32), (30, 25), (11, 7))
    val btype_imm = Seq((52, 32), (7, 7), (30, 25), (11, 8), (1, 0))
    val utype_imm = Seq((32, 32), (31, 12), (12, 0))
    val jtype_imm = Seq((44, 32), (19, 12), (20, 20), (30, 25), (24, 21), (1, 0))
    val ntype_imm = Seq((64, 0))
    val rtype_imm = Seq((64, 0))

    //val IMM_EXT = Seq(rtype_imm, itype_imm, stype_imm, btype_imm, utype_imm, jtype_imm, ntype_imm)

    val immediateEncodingsMap = Map((pipelineParams.rtype -> rtype_imm), (pipelineParams.itype -> itype_imm), 
                                    (pipelineParams.stype -> stype_imm), (pipelineParams.btype -> btype_imm), 
                                    (pipelineParams.utype -> utype_imm), (pipelineParams.jtype -> jtype_imm), 
                                    (pipelineParams.ntype -> ntype_imm))

    def IMM_EXT(machineInstruction: chisel3.UInt, type_w: chisel3.UInt): chisel3.UInt = {

        val immediateMap = typeSeq.map(instructionType => {
            val immediate = Cat(immediateEncodingsMap(instructionType).map( imm_map => {
                imm_map match {
                    case (x, 32) => Fill(x, machineInstruction(31))
                    case (x, 0)  => 0.U(x.W)
                    case (x, y)  => machineInstruction(x, y)
                }
            }))
            (instructionType.U, immediate)
        })

        mapInputToOutput(immediateMap, 0.U(64.W), (x: chisel3.UInt, y: chisel3.UInt) => x === y)(type_w)

    }
}