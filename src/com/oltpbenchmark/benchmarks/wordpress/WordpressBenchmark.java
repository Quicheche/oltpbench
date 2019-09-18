package com.oltpbenchmark.benchmarks.wordpress;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.wordpress.procedures.AddNewPost;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WordpressBenchmark extends BenchmarkModule {

    private static final Logger LOG = Logger.getLogger(WordpressBenchmark.class);

    public WordpressBenchmark(WorkloadConfiguration workConf) {

        super("wordpress", workConf, true);
    }

    @Override
    protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl(boolean verbose) throws IOException {
        LOG.info(String.format("Initializing %d %s", this.workConf.getTerminals(), WordpressWorker.class.getSimpleName()));

        List<Worker<? extends BenchmarkModule>> workers = new ArrayList<Worker<? extends BenchmarkModule>>();
        for (int i = 0; i < this.workConf.getTerminals(); ++i) {
            WordpressWorker worker = new WordpressWorker(this, i);
            workers.add(worker);
        } // FOR
        return workers;
    }

    @Override
    protected Loader<WordpressBenchmark> makeLoaderImpl() throws SQLException {
        return new WordpressLoader(this);

    }

    @Override
    protected Package getProcedurePackageImpl() {
        return (AddNewPost.class.getPackage());
    }
}
