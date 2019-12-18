import {Component} from '@angular/core';
import {RequestService} from 'process/api/request.service';
import {EnvironmentHelperService} from 'app/services/environment.helper.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Environment} from 'process/model/environment';
import {ApprovalRequest} from 'process/model/approvalRequest';


@Component({
  selector: 'app-approval-request',
  templateUrl: './approval-request.component.html',
  styleUrls: ['./approval-request.component.scss']
})
export class ApprovalRequestComponent {


  constructor(
    private client: RequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router,
    route: ActivatedRoute
  ) {
    this.userId = route.snapshot.queryParams['userId'];
    const requestId: string = route.snapshot.paramMap.get('requestId');
    this.envProvider.env().subscribe(e => this.environment = e);

    this.client.getApprovalRequest(this.userId, requestId).subscribe(
      approvalRequest => {
        this.approvalRequest = approvalRequest;
      }, error => {
        console.log('Error loading approval request with id', requestId);
      }
    );
  }

  approvalRequest: ApprovalRequest = ApprovalRequestComponent.emptyApprovalRequest();
  environment: Environment = this.envProvider.none();
  userId: string;

  private static emptyApprovalRequest(): ApprovalRequest {
    return {
      applicant: '',
      amount: '',
      id: 'undefined',
      subject: '',
      currency: 'EUR'
    };
  }

  tasklist() {
    this.router.navigate(['/externalRedirect', {externalUrl: this.environment.tasklistUrl}], {
      skipLocationChange: true,
    });
  }

}
