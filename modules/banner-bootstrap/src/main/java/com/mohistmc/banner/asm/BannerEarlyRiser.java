package com.mohistmc.banner.asm;

import com.chocohead.mm.api.ClassTinkerers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Map;

public class BannerEarlyRiser implements Runnable {

    @Override
    public void run() {
        ClassTinkerers.addTransformation("com.mojang.brigadier.tree.CommandNode", node -> {
            {
                FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE, "CURRENT_COMMAND", "Lcom/mojang/brigadier/tree/CommandNode;", null, null);
                node.fields.add(fieldNode);
                for (var method : node.methods) {
                    if (method.name.equals("canUse")) {
                        for (var instruction : method.instructions) {
                            if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE || instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                var assign = new InsnList();
                                assign.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                assign.add(new FieldInsnNode(Opcodes.PUTSTATIC, "com/mojang/brigadier/tree/CommandNode", fieldNode.name, fieldNode.desc));
                                method.instructions.insertBefore(instruction, assign);
                                var reset = new InsnList();
                                reset.add(new InsnNode(Opcodes.ACONST_NULL));
                                reset.add(new FieldInsnNode(Opcodes.PUTSTATIC, "com/mojang/brigadier/tree/CommandNode", fieldNode.name, fieldNode.desc));
                                method.instructions.insert(instruction, assign);
                                break;
                            }
                        }
                    }
                }
            }
            {
                var removeCommand = new MethodNode();
                removeCommand.access = Opcodes.ACC_PUBLIC;
                removeCommand.name = "removeCommand";
                removeCommand.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "children", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "literals", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "arguments", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new InsnNode(Opcodes.RETURN));
                node.methods.add(removeCommand);
            }
        });

        for (String className : EnumDefinalizer.ENUM) {
            ClassTinkerers.addTransformation(className.replace("/", "."), (node) -> {
                new EnumDefinalizer().processClass(node);
            });
        }
    }
}