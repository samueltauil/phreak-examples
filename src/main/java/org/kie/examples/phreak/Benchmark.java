package org.kie.examples.phreak;

import java.util.concurrent.TimeUnit;

import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.examples.phreak.util.DataGenerator;
import org.kie.internal.builder.conf.RuleEngineOption;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Fork(jvmArgsAppend = { "-server", "-Xmx2048m", "-Xms2048m" })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Benchmark {

    @Param({ "10000" })
    private long numOfTransactions;

    @Param({ "phreak", "reteoo" })
    private String ruleEngine;

    private final KieContainer kcontainer = KieServices.Factory.get().getKieClasspathContainer();
    private KieBaseConfiguration kconfig;
    private DataGenerator generator;

    @Setup
    public void prepare() {
        this.generator = new DataGenerator(this.numOfTransactions);
        this.kconfig = KieServices.Factory.get().newKieBaseConfiguration();
        if (this.ruleEngine.equals("phreak")) {
            this.kconfig.setOption(RuleEngineOption.PHREAK);
        } else {
            this.kconfig.setOption(RuleEngineOption.RETEOO);
        }
    }

    @TearDown
    public void finish() {
        // nothing to do
    }

    private void benchmark(final BenchmarkType type) {
        final KieSession ksession = this.kcontainer.newKieBase(type.getKieBaseName(), this.kconfig).newKieSession();
        type.execute(this.generator, ksession);
        ksession.dispose();
    }

    @GenerateMicroBenchmark
    public void modification() {
        this.benchmark(BenchmarkType.MODIFICATION);
    }

    @GenerateMicroBenchmark
    public void grouping() {
        this.benchmark(BenchmarkType.GROUPING);
    }

    @GenerateMicroBenchmark
    public void laziness3() {
        this.benchmark(BenchmarkType.LAZINESS3);
    }

    @GenerateMicroBenchmark
    public void laziness6() {
        this.benchmark(BenchmarkType.LAZINESS6);
    }
}