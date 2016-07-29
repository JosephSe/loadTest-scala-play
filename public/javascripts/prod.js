var app = angular.module('StarterApp', ['ngMaterial', 'ngMessages', 'ngMdIcons']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$http', '$log', '$mdMedia', '$mdUtil', '$interval',
        function($rootScope, $scope, $http, $log, $mdMedia, $mdUtil, $interval) {
            var hostName = window.location.host;
            $scope.searchEnabled = false;
            $scope.dataLoading = true;
            $scope.screenRefreshTimeout = 60000;
            $scope.charts = ['Pie'];
            $scope.jobChartData = [];
            $scope.serverResponse = {};
            $scope.gaugeData = {};
            $scope.windowWidth = $(window).width();
            var chartLoaded = false;
            //new
            google.charts.load('current', {
                'packages': ['annotationchart', 'line', 'gauge']
            });
            //            google.charts.setOnLoadCallback(drawLogScales);

            $scope.initChart = function() {
                if(!chartLoaded)
                    $scope.dataLoading = true;
                $http({
                    method: 'GET',
                    url: '/jenkins/job/history/all'
                }).then(function successCallback(response) {
                    $scope.serverResponse = response.data;
                    getCount();
                    //                drawLogScales(response.data);
                }, function errorCallback(response) {});
            }

            function getCount() {
                $http({
                    method: 'GET',
                    url: '/jenkins/job/history/count'
                }).then(function successCallback(response) {
                    $scope.gaugeData = response.data;
                    $scope.dataLoading = false;
                    if(!chartLoaded) {
                        google.charts.setOnLoadCallback(drawLogScales);
                        chartLoaded = true;
                    } else {
                        drawLogScales();
                    }
                }, function errorCallback(response) {});
            }

            function loadData(response) {
                var labels = ["passed", "skipped", "failed"];
                angular.forEach(response, function(value) {
                    value.labels = labels;
                    //              value.running = true;
                    value.pieData = [];
                    value.pieData.push(value.totalCount - value.failCount - value.skipCount);
                    value.pieData.push(value.skipCount);
                    value.pieData.push(value.failCount);
                    $scope.jobChartData.push(value);
                })
            }

            function drawLogScales() {
                var response = $scope.serverResponse;
                //                      var tmp = new google.visualization.DataTable();
                var data = new google.visualization.DataTable();
                var tempArray = [];
                data.addColumn('number', 'Build number');
                var keysArray = [];
                angular.forEach(response, function(value, key) {
                    data.addColumn('number', key);
                    keysArray.push(key);
                });
                var size = response.size;
                data.addRows(100);

                for (i = 0; i < 100; i++) {
                    data.setCell(i, 0, i)
                    for (j = 0; j < keysArray.length; j++) {
                        data.setCell(i, j + 1, (j / 2) + getStatusCode(response[keysArray[j]][i]))
                    }
                }

                var options = {
                    width: $scope.windowWidth,
                    // chart: {
                    //     title: 'Production system status'
                    // },
                    hAxis: {
                        // title: 'System Status',
                        baselineColor: '#fff',
                        gridlineColor: '#fff'
                            // logScale: false
                    },
                    vAxis: {
                        baselineColor: '#fff',
                        gridlineColor: '#fff',
                        textPosition: 'none'
                    },
                    colors: ['#097138', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360', '#97BBCD'],
                    //                    smoothLine: true,
                    animation: {
                        duration: 1000,
                        easing: 'inAndOut',
                        startup: true
                    },
                    chartArea: {
                        left: 20,
                        top: 15,
                        width: '75%',
                        height: '85%'
                    }
                };

                var chart = new google.visualization.LineChart(document.getElementById('coreChart_div'));
                chart.draw(data, options);
                drawGauge();
            }

            function drawGauge() {
                var gData = $scope.gaugeData;
                var dataArray = [];
                var emptyArray = [];
                dataArray.push(['Label', 'Value']);
                emptyArray.push(['Label', 'Value']);
                angular.forEach(gData, function(value, key) {
                    dataArray.push([key, value]);
                    emptyArray.push([key, 0]);
                });
                var options = {
                    width: $scope.windowWidth,
                    height: 190,
                    //                  width: 800, height: 200,
                    greenFrom: 90,
                    greenTo: 100,
                    yellowFrom: 60,
                    yellowTo: 90,
                    redFrom: 0,
                    redTo: 60,
                    minorTicks: 10,
                    chartArea: {
                        width: '75%',
                        height: '85%'
                    },
                    animation: {
                        duration: 3000,
                        easing: 'inAndOut',
                        startup: true
                    },

                };

                var chart = new google.visualization.Gauge(document.getElementById('gagueChart_div'));
                var emptyData = google.visualization.arrayToDataTable(emptyArray);
                chart.draw(emptyData, options);

                var chartData = google.visualization.arrayToDataTable(dataArray);
                setTimeout(chart.draw(chartData, options), 3000);

                //                $('#gagueChart_div svg text').attr('y', 100);
                $('#gagueChart_div td').addClass('gauge');
                $('#gagueChart_div svg').find('text:first').attr('y', 200);
                $('#gagueChart_div svg').find('text:first').addClass('gauge-text');
            }

            function getStatusCode(job) {
                var status = job.result
                if (status === "SUCCESS" || status === "BUILDING") return 1
                else if (status === "ABORTED") return 0
                else if (status === "FAILURE") return -.5
            }

//            setInterval($scope.initChart(), 5000);
            // $interval($scope.initChart(), 500);
            $interval(function() {
                $scope.initChart();
            }, $scope.screenRefreshTimeout);
        }
    ])
    .config(function($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('blue-grey')
            .accentPalette('orange')
            .warnPalette('red')
            .backgroundPalette('grey');
    });