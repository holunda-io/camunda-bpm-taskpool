export interface AppInterface {
	'key': string;
	'prozess': string;
	'taskName': string;
	'taskDescription'?: string;
	'creationDate': Date;
	'dueDate': Date;
	'modifiedAt': Date;
	'modifiedBy'?: Date;
	'priority': 'normal' | 'low' | 'high' | 'blocker';
	'indicator': 'claimed' | 'group' | 'claimed by other';
	'candidateGroups'?: string;
	'attributes'?: string;
	'targetUrl'?: string;
	'surce'?: string;
}
