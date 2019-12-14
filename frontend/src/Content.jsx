import React from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import ScreenList from './screens/ScreenList';
import ScreenView from './screens/ScreenView';
import DynamicContent from './DynamicContent';
import ExhibitsList from './exhibits/ExhibitsList';
import ExhibitView from './exhibits/ExhibitView';
import TourList from './tours/TourList';
import TourView from './tours/TourView';
import LanguageContext from './languageContext';
import ExhibitPlayer from './templates/ExhibitPlayer';
import Imprint from './templates/Imprint';

export default function Content() {
  return (
    <Switch>
      {/* 
        Run the requests with language code via the LanguageContent component
        to set the found language in the context.
       */}
      <Route path="/:lang(en|de)" component={LanguageContent} />
      {/* Redirect / => /en/welcome */}
      <Route exact path="/" component={toWelcomePage} />
    </Switch>
  );
}

function LanguageContent({ match }) {
  let lang = match.params.lang;
  // Provide the found language param to the language context
  return (
    <LanguageContext.Provider value={lang}>
      <Switch>
        <Route exact path="/:lang(en|de)/exhibits" component={ExhibitsList} />
        <Route exact path="/:lang(en|de)/exhibits/:id" component={ExhibitView} />

        <Route exact path="/:lang(en|de)/tours" component={TourList} />
        <Route exact path="/:lang(en|de)/tours/:id" component={TourView} />

        <Route exact path="/:lang(en|de)/player/:id" component={ExhibitPlayer} />

        <Route path="/:lang(en|de)/screens/" exact component={ScreenList} />
        <Route path="/:lang(en|de)/screens/:id" exact component={ScreenView} />

        <Route path="/:lang(en|de)/imprint/" component={Imprint} />
        {/* 
          All other requests will be handled by the DynamicContent component.
          It will try to load the content for the given path from Mesh and use
          a matching template to render the retrieved content.
          
          Note the * at the end of the path route. This will allow for multiple
          path segments to be catched by the route.
          */}
        <Route path="/:lang(en|de)/:path*" component={DynamicContent} />
      </Switch>
    </LanguageContext.Provider>
  )
}

/**
 * Redirect the request to the english welcome page.
 */
function toWelcomePage() {
  return (
    <Redirect to='/en/welcome' />
  );
}
