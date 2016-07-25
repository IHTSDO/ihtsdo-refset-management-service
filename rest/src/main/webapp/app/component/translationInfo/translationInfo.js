// Translation Info directive
// e.g. <div translation-info translation='translation' form='long' ></div>
tsApp
  .directive(
    'translationInfo',
    [
      '$uibModal',
      'utilService', 'projectService',
      function($uibModal, utilService, projectService) {
        console.debug('configure translationInfo directive');
        return {
          restrict : 'A',
          scope : {
            translation : '=',
            form : '@'
          },
          templateUrl : 'app/component/translationInfo/translationInfo.html',
          controller : [
            '$scope',
            function($scope) {

              $scope.metadata = {
                terminologyNames : {}
              };

              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);

              };

              // Return the name for a terminology
              $scope.getTerminologyName = function(terminology) {
                return $scope.metadata.terminologyNames[terminology];
              };

              // Get $scope.metadata.terminologies, also loads
              // versions for the first edition in the list
              $scope.getTerminologyEditions = function() {
                projectService
                  .getTerminologyEditions()
                  .then(
                    function(data) {
                      $scope.metadata.terminologies = data.terminologies;
                      // Look up all versions
                      for (var i = 0; i < data.terminologies.length; i++) {
                        $scope.metadata.terminologyNames[data.terminologies[i].terminology] = data.terminologies[i].name;
                      }
                    });

              };

              // Initialize
              $scope.getTerminologyEditions();

              // end
            } ]
        };

      } ]);

// Language list directive
// e.g. <div languages></div>
tsApp.directive('languages', [ 'utilService', function(utilService) {
  console.debug('configure langauges directive');
  return {
    restrict : 'A',
    scope : {
      translation : '='
    },
    templateUrl : 'app/component/translationInfo/languages.html',
    controller : [ '$scope', function($scope) {
      $scope.languages = [ {
        value : 'cs',
        name : 'Czech - cs'
      }, {
        value : 'da',
        name : 'Danish - da'
      }, {
        value : 'nl',
        name : 'Dutch - nl'
      }, {
        value : 'et',
        name : 'Estonian - et'
      }, {
        value : 'he',
        name : 'Hebrew (modern) - he'
      }, {
        value : 'is',
        name : 'Icelandic - is'
      }, {
        value : 'lt',
        name : 'Lithuanian - lt'
      }, {
        value : 'pl',
        name : 'Polish - pl'
      }, {
        value : 'pt',
        name : 'Portuguese - pt'
      }, {
        value : 'sk',
        name : 'Slovak - sk'
      }, {
        value : 'sl',
        name : 'Slovene - sl'
      }, {
        value : 'es',
        name : 'Spanish - es'
      }, {
        value : 'sv',
        name : 'Swedish - sv'
      } ];

      // end
    } ]
  };

  /**
   * All languages { value : 'ab', name : 'Abkhaz - ab' }, { value : 'aa', name :
   * 'Afar - aa' }, { value : 'af', name : 'Afrikaans - af' }, { value : 'ak',
   * name : 'Akan - ak' }, { value : 'sq', name : 'Albanian - sq' }, { value :
   * 'am', name : 'Amharic - am' }, { value : 'ar', name : 'Arabic - ar' }, {
   * value : 'an', name : 'Aragonese - an' }, { value : 'hy', name : 'Armenian -
   * hy' }, { value : 'as', name : 'Assamese - as' }, { value : 'av', name :
   * 'Avaric - av' }, { value : 'ae', name : 'Avestan - ae' }, { value : 'ay',
   * name : 'Aymara - ay' }, { value : 'az', name : 'Azerbaijani - az' }, {
   * value : 'bm', name : 'Bambara - bm' }, { value : 'ba', name : 'Bashkir -
   * ba' }, { value : 'eu', name : 'Basque - eu' }, { value : 'be', name :
   * 'Belarusian - be' }, { value : 'bn', name : 'Bengali, Bangla - bn' }, {
   * value : 'bh', name : 'Bihari - bh' }, { value : 'bi', name : 'Bislama - bi' }, {
   * value : 'bs', name : 'Bosnian - bs' }, { value : 'br', name : 'Breton - br' }, {
   * value : 'bg', name : 'Bulgarian - bg' }, { value : 'my', name : 'Burmese -
   * my' }, { value : 'ca', name : 'Catalan - ca' }, { value : 'ch', name :
   * 'Chamorro - ch' }, { value : 'ce', name : 'Chechen - ce' }, { value : 'ny',
   * name : 'Chichewa, Chewa, Nyanja - ny' }, { value : 'zh', name : 'Chinese -
   * zh' }, { value : 'cv', name : 'Chuvash - cv' }, { value : 'kw', name :
   * 'Cornish - kw' }, { value : 'co', name : 'Corsican - co' }, { value : 'cr',
   * name : 'Cree - cr' }, { value : 'hr', name : 'Croatian - hr' }, { value :
   * 'cs', name : 'Czech - cs' }, { value : 'da', name : 'Danish - da' }, {
   * value : 'dv', name : 'Divehi, Dhivehi, Maldivian - dv' }, { value : 'nl',
   * name : 'Dutch - nl' }, { value : 'dz', name : 'Dzongkha - dz' }, { value :
   * 'en', name : 'English - en' }, { value : 'eo', name : 'Esperanto - eo' }, {
   * value : 'et', name : 'Estonian - et' }, { value : 'ee', name : 'Ewe - ee' }, {
   * value : 'fo', name : 'Faroese - fo' }, { value : 'fj', name : 'Fijian - fj' }, {
   * value : 'fi', name : 'Finnish - fi' }, { value : 'fr', name : 'French - fr' }, {
   * value : 'ff', name : 'Fula, Fulah, Pulaar, Pular - ff' }, { value : 'gl',
   * name : 'Galician - gl' }, { value : 'lg', name : 'Ganda - lg' }, { value :
   * 'ka', name : 'Georgian - ka' }, { value : 'de', name : 'German - de' }, {
   * value : 'el', name : 'Greek (modern) - el' }, { value : 'gn', name :
   * 'Guaraní - gn' }, { value : 'gu', name : 'Gujarati - gu' }, { value : 'ht',
   * name : 'Haitian, Haitian Creole - ht' }, { value : 'ha', name : 'Hausa -
   * ha' }, { value : 'he', name : 'Hebrew (modern) - he' }, { value : 'hz',
   * name : 'Herero - hz' }, { value : 'hi', name : 'Hindi - hi' }, { value :
   * 'ho', name : 'Hiri Motu - ho' }, { value : 'hu', name : 'Hungarian - hu' }, {
   * value : 'is', name : 'Icelandic - is' }, { value : 'io', name : 'Ido - io' }, {
   * value : 'ig', name : 'Igbo - ig' }, { value : 'id', name : 'Indonesian -
   * id' }, { value : 'ia', name : 'Interlingua - ia' }, { value : 'ie', name :
   * 'Interlingue - ie' }, { value : 'iu', name : 'Inuktitut - iu' }, { value :
   * 'ik', name : 'Inupiaq - ik' }, { value : 'ga', name : 'Irish - ga' }, {
   * value : 'it', name : 'Italian - it' }, { value : 'ja', name : 'Japanese -
   * ja' }, { value : 'jv', name : 'Javanese - jv' }, { value : 'kl', name :
   * 'Kalaallisut, Greenlandic - kl' }, { value : 'kn', name : 'Kannada - kn' }, {
   * value : 'kr', name : 'Kanuri - kr' }, { value : 'ks', name : 'Kashmiri -
   * ks' }, { value : 'kk', name : 'Kazakh - kk' }, { value : 'km', name :
   * 'Khmer - km' }, { value : 'ki', name : 'Kikuyu, Gikuyu - ki' }, { value :
   * 'rw', name : 'Kinyarwanda - rw' }, { value : 'rn', name : 'Kirundi - rn' }, {
   * value : 'kv', name : 'Komi - kv' }, { value : 'kg', name : 'Kongo - kg' }, {
   * value : 'ko', name : 'Korean - ko' }, { value : 'ku', name : 'Kurdish - ku' }, {
   * value : 'kj', name : 'Kwanyama, Kuanyama - kj' }, { value : 'ky', name :
   * 'Kyrgyz - ky' }, { value : 'lo', name : 'Lao - lo' }, { value : 'la', name :
   * 'Latin - la' }, { value : 'lv', name : 'Latvian - lv' }, { value : 'li',
   * name : 'Limburgish, Limburgan, Limburger - li' }, { value : 'ln', name :
   * 'Lingala - ln' }, { value : 'lt', name : 'Lithuanian - lt' }, { value :
   * 'lu', name : 'Luba-Katanga - lu' }, { value : 'lb', name : 'Luxembourgish,
   * Letzeburgesch - lb' }, { value : 'mk', name : 'Macedonian - mk' }, { value :
   * 'mg', name : 'Malagasy - mg' }, { value : 'ms', name : 'Malay - ms' }, {
   * value : 'ml', name : 'Malayalam - ml' }, { value : 'mt', name : 'Maltese -
   * mt' }, { value : 'gv', name : 'Manx - gv' }, { value : 'mi', name : 'Māori -
   * mi' }, { value : 'mr', name : 'Marathi (Marāṭhī) - mr' }, { value : 'mh',
   * name : 'Marshallese - mh' }, { value : 'mn', name : 'Mongolian - mn' }, {
   * value : 'na', name : 'Nauruan - na' }, { value : 'nv', name : 'Navajo,
   * Navaho - nv' }, { value : 'ng', name : 'Ndonga - ng' }, { value : 'ne',
   * name : 'Nepali - ne' }, { value : 'nd', name : 'Northern Ndebele - nd' }, {
   * value : 'se', name : 'Northern Sami - se' }, { value : 'no', name :
   * 'Norwegian - no' }, { value : 'nb', name : 'Norwegian Bokmål - nb' }, {
   * value : 'nn', name : 'Norwegian Nynorsk - nn' }, { value : 'ii', name :
   * 'Nuosu - ii' }, { value : 'oc', name : 'Occitan - oc' }, { value : 'oj',
   * name : 'Ojibwe, Ojibwa - oj' }, { value : 'cu', name : 'Old Church
   * Slavonic, Church Slavonic, Old Bulgarian - cu' }, { value : 'or', name :
   * 'Oriya - or' }, { value : 'om', name : 'Oromo - om' }, { value : 'os', name :
   * 'Ossetian, Ossetic - os' }, { value : 'pi', name : 'Pāli - pi' }, { value :
   * 'pa', name : 'Panjabi, Punjabi - pa' }, { value : 'ps', name : 'Pashto,
   * Pushto - ps' }, { value : 'fa', name : 'Persian (Farsi) - fa' }, { value :
   * 'pl', name : 'Polish - pl' }, { value : 'pt', name : 'Portuguese - pt' }, {
   * value : 'qu', name : 'Quechua - qu' }, { value : 'ro', name : 'Romanian -
   * ro' }, { value : 'rm', name : 'Romansh - rm' }, { value : 'ru', name :
   * 'Russian - ru' }, { value : 'sm', name : 'Samoan - sm' }, { value : 'sg',
   * name : 'Sango - sg' }, { value : 'sa', name : 'Sanskrit (Saṁskṛta) - sa' }, {
   * value : 'sc', name : 'Sardinian - sc' }, { value : 'gd', name : 'Scottish
   * Gaelic, Gaelic - gd' }, { value : 'sr', name : 'Serbian - sr' }, { value :
   * 'sn', name : 'Shona - sn' }, { value : 'sd', name : 'Sindhi - sd' }, {
   * value : 'si', name : 'Sinhala, Sinhalese - si' }, { value : 'sk', name :
   * 'Slovak - sk' }, { value : 'sl', name : 'Slovene - sl' }, { value : 'so',
   * name : 'Somali - so' }, { value : 'nr', name : 'Southern Ndebele - nr' }, {
   * value : 'st', name : 'Southern Sotho - st' }, { value : 'es', name :
   * 'Spanish - es' }, { value : 'su', name : 'Sundanese - su' }, { value :
   * 'sw', name : 'Swahili - sw' }, { value : 'ss', name : 'Swati - ss' }, {
   * value : 'sv', name : 'Swedish - sv' }, { value : 'tl', name : 'Tagalog -
   * tl' }, { value : 'ty', name : 'Tahitian - ty' }, { value : 'tg', name :
   * 'Tajik - tg' }, { value : 'ta', name : 'Tamil - ta' }, { value : 'tt', name :
   * 'Tatar - tt' }, { value : 'te', name : 'Telugu - te' }, { value : 'th',
   * name : 'Thai - th' }, { value : 'bo', name : 'Tibetan Standard, Tibetan,
   * Central - bo' }, { value : 'ti', name : 'Tigrinya - ti' }, { value : 'to',
   * name : 'Tonga (Tonga Islands) - to' }, { value : 'ts', name : 'Tsonga - ts' }, {
   * value : 'tn', name : 'Tswana - tn' }, { value : 'tr', name : 'Turkish - tr' }, {
   * value : 'tk', name : 'Turkmen - tk' }, { value : 'tw', name : 'Twi - tw' }, {
   * value : 'uk', name : 'Ukrainian - uk' }, { value : 'ur', name : 'Urdu - ur' }, {
   * value : 'ug', name : 'Uyghur - ug' }, { value : 'uz', name : 'Uzbek - uz' }, {
   * value : 've', name : 'Venda - ve' }, { value : 'vi', name : 'Vietnamese -
   * vi' }, { value : 'vo', name : 'Volapük - vo' }, { value : 'wa', name :
   * 'Walloon - wa' }, { value : 'cy', name : 'Welsh - cy' }, { value : 'fy',
   * name : 'Western Frisian - fy' }, { value : 'wo', name : 'Wolof - wo' }, {
   * value : 'xh', name : 'Xhosa - xh' }, { value : 'yi', name : 'Yiddish - yi' }, {
   * value : 'yo', name : 'Yoruba - yo' }, { value : 'za', name : 'Zhuang,
   * Chuang - za' }, { value : 'zu', name : 'Zulu - zu'
   */

} ]);
