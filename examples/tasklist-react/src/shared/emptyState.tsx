import * as React from "react";

import Paper from "@material-ui/core/Paper";
interface EmptyStateProps {}

export const EmptyStateTaskList = (props: any) => {
  return (
    <React.Fragment>
      <div className="empty-task-list">
        <p>
          Ein klein bisschen <br />
          Geduld noch. <br />
          <br />
          Die Daten für <br />
          die Darstellung der <br />
          Taskliste werden <br />
          gerade hübsch <br />
          gemacht...
        </p>
      </div>
    </React.Fragment>
  );
};

export const EmptyStateLoginPage = (props: any) => {
  return (
    <React.Fragment>
      <Paper>
        <div className="empty-login">
          <p>
            Die Holisticon <br />
            Camunda TaskList
            <br />
            steht gleich nach <br />
            dem Einloggen <br />
            bereit.
          </p>
        </div>
      </Paper>
    </React.Fragment>
  );
};
