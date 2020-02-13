package com.gentics.mesh.alexa.dagger;

import javax.inject.Singleton;

import com.gentics.mesh.alexa.MuseTechSkill;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {

	SkillConfig skillConfig();
	
	MuseTechSkill skill();

	@Component.Builder
	interface Builder {
		@BindsInstance
		Builder config(SkillConfig config);

		AppComponent build();
	}
}
