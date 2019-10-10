import React, { useContext } from 'react';
import { LinkContainer } from 'react-router-bootstrap'
import { Nav } from 'react-bootstrap';
import LanguageContext from '../languageContext';

export default function LanguageToggle({ match, languages }) {
    let lang = useContext(LanguageContext);
    return (
        <>
            <LinkContainer to={replaceLangPrefixInUrl(match, lang, languages)}>
                <Nav.Link>{switchedLang(lang)}</Nav.Link>
            </LinkContainer>
        </>
    );
}

/**
 * Generate the alternative language link
 * 
 * @param match Current routing information
 * @param lang Current active language
 * @param languages Optional language information which may contain information for language specific paths
 */
function replaceLangPrefixInUrl(match, lang, languages) {
    let newLang = switchedLang(lang);
    if (match.path.startsWith("/:lang")) {
        if (languages !== undefined) {
            let paths = languages.filter(e => e.language === newLang)
                .map(e => {
                    return e.path;
                });
            if (paths.length > 0) {
                return "/" + newLang  + paths[0];
            }

        }
        let shortPath = match.url.substring(4);
        return "/" + newLang + "/" + shortPath;
    } else {
        return match.url;
    }
}

function switchedLang(lang) {
    if (lang === "de") {
        return "en";
    } else {
        return "de";
    }
}

