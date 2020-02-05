import React, { useEffect, useContext } from 'react';
import { Navbar, Nav, Container } from 'react-bootstrap';
import logoFile from '../img/logo.svg';
import LanguageToggle from './LanguageToogle'
import { Route } from 'react-router-dom';
import { LinkContainer } from 'react-router-bootstrap'
import LanguageContext from '../languageContext';

const logo = {
  src: logoFile,
  alt: 'Blackspring History Museum',
};

const trans = {
  de: {
    price: "Preise",
    price_path: "/de/preise",

    exhibits: "Austellung",
    exhibits_path: "/de/exhibits",
    
    about: "Über uns",
    about_path: "/de/über",

    tours: "Führungen",
    tours_path: "/de/führungen",
    
    history: "Geschichte",
    history_path: "/de/geschichte",
    
    screens: "Screens",
    screens_path: "/de/screens"
  },
  en: {
    price: "Pricing",
    price_path: "/en/pricing",

    exhibits: "Exhibits",
    exhibits_path: "/en/exhibits",

    about: "About",
    about_path: "/en/about",

    tours: "Tours",
    tours_path: "/en/tours",

    history: "History",
    history_path: "/en/history",

    screens: "Screens",
    screens_path: "/en/screens"
  }
}
export default function Navigation({ languages }) {
  let lang = useContext(LanguageContext);
  if (lang === undefined) {
    lang = "en";
  }

  let navScrollRef = React.createRef();
  let navRef = React.createRef();
  useEffect(() => {
    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
    function handleScroll(event) {
      let offsetTop = navScrollRef.current.getBoundingClientRect().top;
      if (offsetTop < -100) {
        navRef.current.classList.add("navbar-shrink");
      } else {
        navRef.current.classList.remove("navbar-shrink");
      }
    }
  }, [navRef, navScrollRef, languages]);

  let i18n = trans[lang];

  return (
    <div id="home" ref={navScrollRef} className="nav-spacer">
      <Navbar id="mainNav" ref={navRef} collapseOnSelect={true} expand="lg" fixed="top" variant="dark">
        <Container>
          <Navbar.Brand href="/"><img className="logo img-responsive" src={logo.src} alt={logo.alt} /> BMH</Navbar.Brand>
          <Navbar.Toggle aria-controls="responsive-navbar-nav" className="navbar-toggler-right" />
          <Navbar.Collapse id="responsive-navbar-nav">
            <Nav className="ml-auto text-uppercase">
              <LinkContainer to={i18n.exhibits_path}>
                <Nav.Link>{i18n.exhibits}</Nav.Link>
              </LinkContainer>
              <LinkContainer to={i18n.price_path}>
                <Nav.Link>{i18n.price}</Nav.Link>
              </LinkContainer>
              <LinkContainer to={i18n.tours_path}>
                <Nav.Link>{i18n.tours}</Nav.Link>
              </LinkContainer>
              <LinkContainer to={i18n.history_path}>
                <Nav.Link>{i18n.history}</Nav.Link>
              </LinkContainer>
              <LinkContainer to={i18n.screens_path}>
                <Nav.Link>{i18n.screens}</Nav.Link>
              </LinkContainer>
              <LinkContainer to={i18n.about_path}>
                <Nav.Link>{i18n.about}</Nav.Link>
              </LinkContainer>
              <LinkContainer to="/en/ssotest">
                <Nav.Link>SSOTest</Nav.Link>
              </LinkContainer>
              <div className="nav-divider d-none d-lg-block" />
              <Route languages={languages} render={(routeProps) =>
                <LanguageToggle {...routeProps} languages={languages} />}
              />
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
    </div>
  )


}
