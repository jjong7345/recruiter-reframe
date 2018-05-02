### Table Registry
Paginated tables register many subscriptions and events in order to contain all table
logic. If the table required does not need to be paginated, then there is no
need to use any of the registered subscriptions and events.

There are 2 types of paginated tables: frontend and backend pagination. Frontend
paginated tables should be used when the entirety of the dataset is available
from the frontend. Backend paginated tables should be used when pagination is
handled by a backend service and therefore fetching each page requires an ajax
call.

### Common Table Arguments
There are some similarities between frontend/backend paginated tables. The following
arguments are accepted for both tables:

- `table-key`: Fully qualified key for table (defined in table namespace)
- `row-data-fn`: Function that will be called on each record in dataset and return a vector of row data (corresponding to each column)
- `headers`: Collection of maps with the following options:
  - `label`: Label for header
  - `width`: *Optional* Percent width of table
  - `sort`: *Optional* Map of sort-key to asc-sort-fn (ONLY ACCEPTABLE FOR `frontend-pagination-table`)
    - `sort-key`: Key to be used to determine current sort-col
    - `asc-sort-fn`: Function to be used to sort dataset returned from `data-sub` in ascending order (see [sort-fn](https://github.com/TheLadders/recruit-app/blob/master/src/cljs/recruit_app/util/sort.cljs#L38-L46))
- `per-page`: *Optional* Integer denoting how many results to show per page (default is `10`)

### Frontend Pagination
In order to utilize the frontend pagination capabilities, a `frontend-pagination-table`
must be created with the following options:

- `data-sub`: Keyword for subscription that will return collection of all data for the table
- `initial-sort-col`: *Optional* When including sortable headers, will set initial column to sort table (default is first sortable header)
- `initial-sort-dir`: *Optional* When including sortable headers, will set initial direction to sort table (default is `desc`)
- `loading?`: *Optional* Boolean, whether table data is currently being loaded (default is `false`)
- `actions`: *Optional* Vector of maps of button specifications. When actions are provided, a checkbox is added to each row and subs/events are registered to handle checking.
  - `label`: Label for the button
  - `on-click`: Function to be called when button is clicked (Set of checked rows is sent into this function when clicked)

### Backend Pagination
Backend pagination requires the following options:

- `fetch-url`: URL to be called to fetch page of data. Must be POST, accept below request and return below response
  - Request (POST)
    - `limit`: Limit for dataset
    - `offset`: Offset of pagination
  - Response (JSON)
    - `total`: Total records in full dataset
    - `results`: Vector of result data
- `fetch-params`: *Optional* Map of params to be merged to request sent to fetch-url

### Events
When a table is rendered, subs/events will be registered using the table-key.
There are `def`s in the `table` namespace corresponding to each event so that
the events can be fired from outside the component.

For example, if you were creating a table with the key `::table/search` and wanted
to paginate back to the first page, you could dispatch an event like this:
```
(rf/dispatch [(table/set-page-event ::table/search) 0]
```
Any of these events can be fired from outside the component, but it's not advised
to do so unless necessary.