package com.test.arthas;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * VirtualMachine 依赖jdk目录下的 tools.jar
 */
public class App {

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        VirtualMachine.list().forEach(virtualMachineDescriptor -> System.out.printf("pid=%s, displayName=%s, provider=%s%n",
                virtualMachineDescriptor.id(), virtualMachineDescriptor.displayName(), virtualMachineDescriptor.provider()));
        System.out.println("please input pid");
        try (Scanner scanner = new Scanner(System.in)) {
            String line = scanner.nextLine();
            VirtualMachine vm = VirtualMachine.attach(line);
            vm.loadAgent(new File("arthas/arthas-agent/target/arthas-agent.jar").getAbsolutePath());
            do {
                line = scanner.nextLine();
            } while (!"bye".equalsIgnoreCase(line));
            vm.detach();
        }
    }
}
