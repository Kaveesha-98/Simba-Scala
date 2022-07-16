package common

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
    def mapInputToOutput[T <: Data](mapSeq: Seq[String], mapEntries: Map[String , T], default: T, f: (String, T) => chisel3.Bool)( mapInput: T): T = {
        val conditionArray = mapSeq.map(mapCondition => f(mapCondition, mapInput) -> mapEntries(mapCondition))

        MuxCase(default, conditionArray)
    }

    def createResultsToMultiplex(inputSeq: Seq[String], default: chisel3.UInt, f: (String, chisel3.UInt) => chisel3.UInt)(
        machineInstruction: chisel3.UInt, type_w: chisel3.UInt): chisel3.UInt = {

        var resultMap = Map.empty[String, chisel3.UInt]
        
        inputSeq.map(inputType => {
            val result = f(inputType, machineInstruction)
            resultMap = resultMap + (inputType -> result)
        })

        mapInputToOutput(inputSeq, resultMap, default, (x: String, y: chisel3.UInt) => x.U === y)(type_w)

    }

    val typeSeq = Seq(pipelineParams.rtype, pipelineParams.itype, 
                      pipelineParams.stype, pipelineParams.btype, 
                      pipelineParams.utype, pipelineParams.jtype, 
                      pipelineParams.ntype) 

    val opcodeSeq = Seq(pipelineParams.lui, 
                        pipelineParams.auipc, 
                        pipelineParams.jump, 
                        pipelineParams.jumpr, 
                        pipelineParams.cjump, 
                        pipelineParams.load, 
                        pipelineParams.store, 
                        pipelineParams.iops, 
                        pipelineParams.iops32, 
                        pipelineParams.rops, 
                        pipelineParams.rops32, 
                        pipelineParams.system, 
                        pipelineParams.fence, 
                        pipelineParams.amos)

    //opcode -> type_w
    val instructionOpcodeToTypeMap = Map(pipelineParams.lui ->     pipelineParams.utype.U, 
                                         pipelineParams.auipc ->   pipelineParams.utype.U, 
                                         pipelineParams.jump ->    pipelineParams.jtype.U, 
                                         pipelineParams.jumpr ->   pipelineParams.itype.U, 
                                         pipelineParams.cjump ->   pipelineParams.btype.U, 
                                         pipelineParams.load ->    pipelineParams.itype.U, 
                                         pipelineParams.store ->   pipelineParams.stype.U, 
                                         pipelineParams.iops ->    pipelineParams.itype.U, 
                                         pipelineParams.iops32 ->  pipelineParams.itype.U, 
                                         pipelineParams.rops ->    pipelineParams.rtype.U, 
                                         pipelineParams.rops32 ->  pipelineParams.rtype.U, 
                                         pipelineParams.system ->  pipelineParams.itype.U, 
                                         pipelineParams.fence ->   pipelineParams.ntype.U, 
                                         pipelineParams.amos ->    pipelineParams.rtype.U)
	//default :   TYPE=ntype;
    val INS_TYPE_ROM = mapInputToOutput(opcodeSeq, instructionOpcodeToTypeMap, pipelineParams.ntype.U, (x: String, y: chisel3.UInt) => x.U === y)(_)

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

    val immediateEncodingsMap = Map(pipelineParams.rtype -> rtype_imm, pipelineParams.itype -> itype_imm,
                                    pipelineParams.stype -> stype_imm, pipelineParams.btype -> btype_imm,
                                    pipelineParams.utype -> utype_imm, pipelineParams.jtype -> jtype_imm,
                                    pipelineParams.ntype -> ntype_imm)

    val immediateCreation = (instructionType: String, machineInstruction: chisel3.UInt) => {
        Cat(immediateEncodingsMap(instructionType).map {
            case (x, 32) => Fill(x, machineInstruction(31))
            case (x, 0) => 0.U(x.W)
            case (x, y) => machineInstruction(x, y)})
    }

    val IMM_EXT = createResultsToMultiplex(typeSeq, 0.U(64.W), immediateCreation)(_, _)

    val rs1ValidTypes = Seq(pipelineParams.rtype, pipelineParams.itype, pipelineParams.stype, pipelineParams.btype)
    val rs2ValidTypes = Seq(pipelineParams.rtype, pipelineParams.stype, pipelineParams.btype)

    val rs1_sel_mux = createResultsToMultiplex(typeSeq, 0.U(5.W), (instructionType: String, machineInstruction: chisel3.UInt) => {
        if (rs1ValidTypes.contains(instructionType)) machineInstruction(19, 15) else 0.U(5.W) 
    })(_, _)

    val rs2_sel_mux = createResultsToMultiplex(typeSeq, 0.U(5.W), (instructionType: String, machineInstruction: chisel3.UInt) => {
        if (rs2ValidTypes.contains(instructionType)) machineInstruction(24, 20) else 0.U(5.W) 
    })(_, _)

}