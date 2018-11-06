import * as React from "react";

import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import { Table, TableHead, TableRow, TableCell } from "@material-ui/core";

import { withStyles, createStyles } from "@material-ui/core/styles";
import { Theme } from "@material-ui/core/styles/createMuiTheme";

import { EmptyStateTaskList } from "../shared/emptyState";
export interface ITaskListProps {}

const styles = (theme: Theme) =>
  createStyles({
    thead: {
      fontSize: "1.8rem"
    }
  });

class TaskList extends React.Component<any, ITaskListProps, any> {
  state = {
    tasksAvailable: false
  };
  public render() {
    const { classes } = this.props;

    const taskListTable = (
      <React.Fragment>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell className={classes.thead}>ID</TableCell>
            </TableRow>
          </TableHead>
        </Table>
      </React.Fragment>
    );

    return (
      <React.Fragment>
        <Grid container spacing={24} className="base-grid-container">
          <Grid item xs={12}>
            <Paper>
              {this.state.tasksAvailable ? (
                taskListTable
              ) : (
                <EmptyStateTaskList />
              )}
            </Paper>
          </Grid>
        </Grid>
      </React.Fragment>
    );
  }
}

export default withStyles(styles)(TaskList);
