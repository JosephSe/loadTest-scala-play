var app = angular.module('StarterApp', ['ngMaterial', 'ngMessages', 'ngMdIcons', 'ngCookies']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$mdSidenav', '$http', '$log', '$mdBottomSheet', '$cookieStore', '$mdDialog', 'appData', '$mdUtil', 'searchService', '$sce',
        function($rootScope, $scope, $mdSidenav, $http, $log, $mdBottomSheet, $cookieStore, $mdDialog, appData, $mdUtil, searchService, $sce) {
            var hostName = window.location.host;
            appData.hostName = hostName;
            $scope.searchEnabled = false;
            $scope.stompClient = null;
            $scope.data = appData;
            $scope.fileNamePrefix = "";
            $scope.client = appData.client;
            $scope.client.connected = appData.client.connected;
            $scope.client.showAutomation = false;
            $scope.xmls = appData.xmls;
            $scope.zips = appData.zips;
            $scope.xmlContent = appData.xmlContent;
            //			$scope.prettyXML = $sce.trustAsHtml("<h1>new content</h1>");
            $scope.client.allLoaded = appData.client.allLoaded;
            $scope.loadedFile = appData.loadedFile;
            $scope.selectedXMl = appData.selectedXMl;
            $scope.searchText = appData.searchText;
            $scope.clearAll = function() {
                $scope.data.clearXMLs();
                $scope.data.clearLoadedXML();
                console.log("data cleared!");
            }

            $scope.toggleRight = buildToggler('right');

            $scope.toggleSidenav = function(menuId) {
                $mdSidenav(menuId).toggle();
            };

            var ws = new WebSocket("ws://" + hostName + "/ws/broadcast");

            ws.onopen = function() {
                console.log("Server socket has connection made!");
            };

            ws.onmessage = function(message) {
                listener(JSON.parse(message.data));
            };
            ws.onclose = function (ev) {
                $mdDialog.show(
                  $mdDialog.alert()
                    .parent(angular.element(document.querySelector('#popupContainer')))
                    .clickOutsideToClose(true)
                    .title('Connection Lost')
                    .textContent('Client has lost connection with server. Please reload your web-page.')
                    .ariaLabel('Connection Lost')
                    .ok('Got it!')
                    .targetEvent(ev)
                );
            };

            function listener(data) {
                console.log("Received data from websocket: ", data);
                if ($scope.fileNamePrefix === "" || data.fileName.indexOf($scope.fileNamePrefix) === 0) {
                    //    $scope.updateUI(data);
                    if (data.typ === "xml") appData.xmls.push(data);
                    else appData.zips.push(data);
                    $scope.$apply();
                }
            }

            $scope.loadXML = function(file) {
                if(file.typ === "zip") {
                    $scope.data.loadedFile.name = file.name;
                    $scope.data.loadedFile.content = "Preview option is not available for this file type. Please use the download link";
//                    $scope.$apply();
                } else {
                    $scope.data.fileLoading = true;
                    $scope.data.clearLoadedXML;
                    $http.get('/file/get/' + file.typ + "/" + file.uuid).
                    success(function(data) {
                        file.newFile = false;
                        $scope.data.fileLoading = false;
                        $scope.data.loadedFile.name = data.name;
                        var fileContent = $sce.trustAsHtml(data.content);
                        $scope.data.loadedFile.content = $sce.trustAsHtml(data.content);
//                        $scope.$apply();
                    });
                }
            };
            $scope.loadAll = function() {
                appData.client.allLoaded = true;
                $http.get('/response/all?namePrefix=' + $scope.fileNamePrefix)
                    .success(function(data) {
                        appData.clearXMLs();
                        $scope.updateUI(data);
                        appData.client.allLoaded = false;
                    });
            };
            $scope.loadAllFiles = function() {
                appData.searchFile("", appData.fileNames)
            };
            $scope.showListBottomSheet = function($event) {
                $scope.alert = '';
                $mdBottomSheet.show({
                    templateUrl: 'bottomSearch.template.html',
                    controller: 'SearchController',
                    targetEvent: $event
                }).then(function(clickedItem) {
                    $scope.alert = clickedItem.name + ' clicked!';
                });
            };

            $scope.showTopSearch = function($event) {
                $scope.alert = '';
                $mdTopSheet.show({
                    templateUrl: 'topSearch.template.html',
                    controller: 'SearchController',
                    targetEvent: $event
                }).then(function(clickedItem) {
                    $scope.alert = clickedItem.name + ' clicked!';
                });
            };

            /**
             * Build handler to open/close a SideNav; when animation finishes
             * report completion in console
             */
            function buildToggler(navID) {
                var debounceFn = $mdUtil.debounce(function() {
                    $mdSidenav(navID)
                        .toggle()
                        .then(function() {
                            $log.debug("toggle " + navID + " is done");
                        });
                }, 300);

                return debounceFn;
            };

            $scope.showSearchPopup = function(ev) {
                var displayed = $cookieStore.get("searchPopupDisplayed")
                displayed = undefined;
                if (displayed === undefined) {
                    $mdDialog.show({
                            controller: 'DialogController',
                            templateUrl: 'dialog.tmpl.html',
                            parent: angular.element(document.body),
                            targetEvent: ev,
                            clickOutsideToClose: true
                        })
                        .then(function(answer) {
                            $scope.status = 'You said the information was "' + answer + '".';
                        }, function() {
                            $scope.status = 'You cancelled the dialog.';
                        });
                }
            }

            $scope.updateUI = function(xmlData) {
                //                if(!$scope.client.showAutomation && (xmlData.fileName.indexOf('automationTest-') == 0 || xmlData.fileName.indexOf('TSTJ_BBC3_') == 0)) {
                //                    show nothing
                //                } else {
                for (i = 0; i < xmlData.xml.length; i++) {
                    appData.xmls.push(xmlData.xml[i]);
                }
                for (i = 0; i < xmlData.zip.length; i++) {
                    appData.zips.push(xmlData.zip[i]);
                }

                //                    if(xmlData.typ ==="xml") appData.xmls.push(xmlData);
                //                    else appData.zips.push(xmlData);
                //            	}

            };
        }
    ])
    .controller('SearchController', function($scope, $mdBottomSheet, $http, appData, $mdSidenav, $log, searchService) {
        var self = $scope;
        var hostName = appData.hostName
        self.searchData = {};
        self.searchData.fileName = 'JModi';
        self.searchData.creationDate = '2015-06-22';
        self.selectedXML = appData.selectedXML;
        self.searchResult = {};
        self.isResultEmpty = true;
        self.close = function() {
            $mdSidenav('right').close()
                .then(function() {});
        };
        self.searchXML = function() {
            appData.clearArray(self.searchResult);
            $http.get(hostName + 'search/groupByCreatedDate?fileName=' + self.searchData.fileName + "&creationDate=" + self.searchData.creationDate)
                .success(function(data) {
                    self.searchResult = data;
                    self.isResultEmpty = angular.equals({}, data);
                });
        };
        self.viewXML = function(xml) {
            appData.fileLoading = true;
            appData.clearLoadedXML;

            $http.get(hostName + 'search/view?fileName=' + xml.fileName + "&responseReference=" + xml.responseReference)
                .success(function(response) {
                    appData.loadedFile.content = response.data;
                    appData.fileLoading = false;
                    appData.loadedFile.name = response.fileName;

                });
        }
        $scope.$on('searchXMLResponse', function(event, args) {
            $scope.searchFile(args);
        });
        self.searchTextChange = function(text) {};
        self.selectedItemChange = function(item) {
            $log.info('Item changed to ' + JSON.stringify(item));
        };
        self.findFile = function(fileName) {
            searchService.searchFile(fileName, $scope.fileNames);
        }
        $scope.findFile = function(fileName) {
            searchService.searchFile(fileName);
        }

    })
    .controller('DialogController', function($scope, $mdDialog) {
        $scope.hide = function() {
            $mdDialog.hide();
        };
        $scope.cancel = function() {
            $mdDialog.cancel();
        };
        $scope.answer = function(answer) {
            $mdDialog.hide(answer);
        };
    })
    .service('appData', function() {
        var self = this;
        self.stompClient = null;
        self.client = {}
        self.client.connected = true;
        self.xmls = [];
        self.zips = [];
        self.clearXMLs = function() {
            self.clearArray(self.xmls);
            self.clearArray(self.zips);
        };
        self.clearArray = function(array) {
            array.length = 0;
            /*
            			var k = array.length;
            			while (k >= 0) {
            				array.pop();
            				k--;
            			}
            */
        }
        self.clearLoadedXML = function() {
            self.loadedFile.name = "";
            self.loadedFile.content = "";
        };
        self.xmlContent = "";
        self.client.allLoaded = false;
        self.loadedFile = {};
        self.loadedFile.name = "";
        self.loadedFile.content = "";
        self.searchResults = [];
        self.selectedXMl = {};
        self.searchText = "";
        self.fileLoading = false;
    })
    .factory('searchService', ['$http', 'appData', function($http, appData, $scope) {
        var hostName = '/'
        this.searchFile = function(fileName) {
            //			appData.searchResults = [];
            //			$http.get(hostName + 'search?fileName=' + fileName).
            //			success(function(data) {
            //		        appData.searchResults = data;
            //			});
        };
        return this;
    }])
    .config(function($mdThemingProvider) {
        //		$mdThemingProvider.theme('default')
        //			.primaryPalette('deep-purple')
        //			.accentPalette('orange');

        $mdThemingProvider.theme('default')
            .primaryPalette('blue')
            .accentPalette('orange')
            .warnPalette('red')
            .backgroundPalette('grey');
    });