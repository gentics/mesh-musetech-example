import { useState, useEffect } from 'react';
import config from './config.json';

export async function filterExhibits(lang, term) {
  let query = JSON.stringify(createESQuery(term));
  return graphQl(`
  query Exhibits($lang: [String], $query: String) {
    nodes(lang: $lang, query: $query) {
      elements {
        uuid
        path
        ... on Exhibit {
          fields {
            id
            name
            title_image(lang: "en") {
              path
              ... on Image {
                fields {
                  attribution
                  binary {
                    dominantColor
                  }
                }
              }
            }
            public_number
            images {
              node(lang: "en") {
                path
                ... on Image {
                  fields {
                    binary {
                      dominantColor
                    }
                  }
                }
              }
            }
            audio {
              path
            }
          }
        }
      }
    }
  }  
  `, { lang, query }).then(response => response);
}

function createESQuery(term) {
  let query =
  {
    "sort": {
      "created": {
        "order": "asc"
      }
    },
    "query": {
      "bool": {
        "must": [
          {
            "match": {
              "schema.name.raw": "Exhibit"
            }
          },
          {
            "match": {
              "fields.name": term
            }
          }
        ]
      }
    }
  };
  return query;
}

export async function loadContentByPath(path) {
  return graphQl(`
  query Content($path: String) {
    node(path: $path) {
      uuid
      version
      languages {
        path
        language
      }
      schema {
        name
      }
      ... on ContentPage {
        fields {
          slug
          intro
          title
          text
        }
      }
      ... on WelcomePage {
        fields {
          slug
          intro
          text
          title
          openinghours {
              uuid
              ... on OpeningHour {
                fields {
                  days
                  from 
                  to
                }
              }
          }
          museum {
            uuid
            ... on MuseumInfo {
              fields {
                name
                email
                phone
                street
                city
              }
            }
          }
        }
      }
      ... on HistoryPage {
        fields {
          title
          headline
          intro
          timeline {
            uuid
            ... on HistoryEpisode {
              fields {
                time
                subheading
                text
                image(lang: "en") {
                  path
                }
              }
            }
          }
        }
      }
      ... on AdmissionPage {
        fields {
          title
          headline
          intro
          types {
            uuid
            ... on AdmissionInfo {
              fields {
                title
                price
                icon
                description
              }
            }
          }
        }
      }
    }
  }  
  `, { "path": "/" + path }).then(response => response.node);
}


export async function getTour(id, lang) {
  return graphQl(`
  query Tour($path: String) {
    node(path: $path) {
      uuid
      path
      ... on Tour {
        fields {
          public_number
          slug
          title
          price
          size
          description
          guides {
            uuid
            node(lang: "en") {
              ... on Person {
                fields {
                  title
                  firstname
                  lastname
                  email
                  image {
                    ... on Image {
                      path
                    }
                  }
                  quote
                }
              }
            }
          }
          image(lang: "en") {
            path
            ... on Image {
              fields {
                attribution
                binary {
                  dominantColor
                }
              }
            }
          }
          dates {
            uuid
            ... on TourDate {
              fields {
                date
                seats
              }
            }
          }
        }
      }
    }
  }  
  `, { "path": "/tours/" + id + ":" + lang }).then(response => response.node);
}


export async function getTours(lang, page) {
  return graphQl(`
  query Tours($lang: [String], $page: Long) {
    node(path: "/tours") {
      children(lang: $lang, page: $page) {
        hasPreviousPage
        hasNextPage
        pageCount
        elements {
          uuid
          path
          ... on Tour {
            fields {
              public_number
              slug
              title
              description
              size
              price
              image(lang: "en") {
                path
                ... on Image {
                  fields {
                    attribution
                    binary {
                      dominantColor
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }   
  `, { lang, page }).then(response => response.node);
}

export async function getExhibit(id, lang) {
  return graphQl(`
  query Exhibit($path: String) {
    node(path: $path) {
      uuid
      path
      ... on Exhibit {
        fields {
          id
          name
          description
          location {
            ... on Location {
              fields {
                building
                section
                level
              }
            }
          }
          title_image(lang: "en") {
            path
            ... on Image {
              fields {
                attribution
                binary {
                  dominantColor
                }
              }
            }
          }
          public_number
          images {
            node(lang: "en") {
              path
            }
          }
          audio {
            path
          }
        }
      }
    }
  }  
  `, { "path": "/exhibits/" + id + ":" + lang }).then(response => response.node);
}

export async function getScreen(id) {
  return graphQl(`
  query Screen($path: String) {
    node(path: $path) {
      uuid
      path
      version
      ... on Screen {
        fields {
          id
          name
          description
          location
          contents {
            type: __typename
            ... on ScreenEvent {
              fields {
                title
                teaser
                tour {
                  uuid
                  ... on Tour {
                    fields {
                      public_number
                      title
                      size
                      duration
                      location
                      dates {
                        uuid
                        ... on TourDate {
                          fields {
                            date
                            seats
                          }
                        }
                      }
                    }
                  }
                }
                image {
                  node {
                    path
                  }
                }
                video {
                  node {
                    path
                  }
                }
              }
            }
            ... on ScreenExhibitPromo {
              fields {
                title
                teaser
                image {
                  node {
                    path
                  }
                }
                video {
                  node {
                    path
                  }
                }
              }
            }
          }
        }
      }
    }
  }     
  `, { "path": "/screens/" + id });
}

export async function loadAdmissionInfo(lang) {
  return graphQl(`
  query admission($lang: [String]){
    node(path: "/pricing") {
      node(lang: $lang) {
        ... on AdmissionPage {
          version
          uuid
          fields {
            types {
              uuid
              ... on AdmissionInfo {
                fields {
                  title
                  price
                }
              }
            }
          }
        }
      }
    }
  }  
  `, { lang }).then(response => response.node);
}

export async function getExhibits(lang, page) {
  return graphQl(`
  query Exhibits($lang: [String], $page: Long) {
    node(path: "/exhibits") {
      children(lang: $lang, page: $page) {
        hasPreviousPage
        hasNextPage
        pageCount
        elements {
          uuid
          path
          ... on Exhibit {
            fields {
              id
              name
              title_image(lang: "en") {
                path
                ... on Image {
                  fields {
                    attribution
                    binary {
                      dominantColor
                    }
                  }
                }
              }
              public_number
              images {
                node(lang: "en") {
                  path
                  ... on Image {
                    fields {
                      binary {
                        dominantColor
                      }
                    }
                  }
                }
              }
              audio {
                path
              }
            }
          }
        }
      }
    }
  }   
  `, { lang, page }).then(response => response.node);
}

export async function getScreens() {
  return graphQl(`
  {
    node(path: "/screens") {
      children(lang: ["en", "de"]) {
        elements {
          uuid
          path
          ... on Screen {
            fields {
              name
              id
              description
              location
              contents {
                __typename
              }
            }
          }
        }
      }
    }
  }    
  `).then(response => response.node);
}

export async function getVideos() {
  return graphQl(`
  {
    node(path: "/videos") {
      children {
        elements {
          uuid
          path
          ... on Video {
            fields {
              filename
              binary {
                fileSize
                mimeType
              }
            }
          }
        }
      }
    }
  }   
  `).then(response => response.node);

}

export function getProject() {
  return get(`/musetech`)
}

export function usePromise(promiseFn, changes) {
  const [state, setState] = useState();

  useEffect(() => {
    promiseFn().then(setState)
  }, [promiseFn])

  return state;
}

function graphQl(query, variables) {
  return post(`/musetech/graphql?version=published`, { query, variables }).then(response => response.data);
}

function get(path) {
  return fetch(`${config.meshUrl}${path}`)
    .then(response => response.json());
}

function post(path, data) {
  //console.log(JSON.stringify(data));
  return fetch(`${config.meshUrl}${path}`, {
    body: JSON.stringify(data),
    method: 'POST'
  }).then(response => response.json());
}
