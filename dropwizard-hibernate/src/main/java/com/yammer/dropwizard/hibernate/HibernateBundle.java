package com.yammer.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T> {
    protected SessionFactory sessionFactory;

    protected final ImmutableList<Class<?>> entities;
    protected final SessionFactoryFactory sessionFactoryFactory;

    protected HibernateBundle(Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entities).build(),
             new SessionFactoryFactory());
    }
    
    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
             new SessionFactoryFactory());
    }

    protected HibernateBundle(ImmutableList<Class<?>> entities,
                              SessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapperFactory().registerModule(new Hibernate4Module());
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final DatabaseConfiguration dbConfig = getDatabaseConfiguration(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities);
        environment.addProvider(new UnitOfWorkResourceMethodDispatchAdapter(sessionFactory));
        environment.addHealthCheck(new SessionFactoryHealthCheck("hibernate",
                                                                 sessionFactory,
                                                                 dbConfig.getValidationQuery()));
        
        sessionFactoryInitHook(this.sessionFactory);
    }

   

	public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Hook allowing for custom Hibernate SessionFactory customization.
     * 
     * Customize the configuration object to add filters or change naming strategies.
     * 
     * @param configuration
     */
    public void configure(org.hibernate.cfg.Configuration configuration) {
    }
    
    /**
     * Hook that will be called once from run() once the SessionFactory is created.
     * 
     * Useful for when you need access to the SessionFactory in another Bundle's
     * intialization code.
     * 
     * 
     * @param sessionFactory2
     */
    protected void sessionFactoryInitHook(SessionFactory sessionFactory2) {
		
	}
}
