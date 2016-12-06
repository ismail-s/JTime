from fabric.api import run, cd

def deploy():
    "Deploy the Rest api & website to production"
    with cd('JTime/JTime-rest'):
        run('git pull')
        run('npm install .')
        with cd('client/jtime-website'):
            run('npm install .')
            run('npm run build')
        run('pm2 reload .')
